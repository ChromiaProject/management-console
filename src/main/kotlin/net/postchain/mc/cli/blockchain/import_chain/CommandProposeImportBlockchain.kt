package net.postchain.mc.cli.blockchain.import_chain

import com.chromia.directory1.common.queries.getContainerData
import com.chromia.directory1.common.queries.getVoterSetInfo
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.queries.getBlockchainInfo
import net.postchain.chain0.model.BlockchainState
import net.postchain.chain0.proposal.ProposalData
import net.postchain.chain0.proposal.ProposalState
import net.postchain.chain0.proposal.getProposal
import net.postchain.chain0.proposal.getProposalIdsByTxRid
import net.postchain.chain0.proposal_blockchain_import.getBlockchainImportProposalId
import net.postchain.chain0.proposal_blockchain_import.proposeImportBlockchainOperation
import net.postchain.chain0.proposal_blockchain_import.proposeImportConfigurationOperation
import net.postchain.client.core.TransactionResult
import net.postchain.client.core.TxRid
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.BlockchainRid
import net.postchain.common.hexStringToByteArray
import net.postchain.common.tx.TransactionStatus
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvDecoder
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.configurationsFileOption
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.nullableProposalDescriptionOption
import net.postchain.mc.cli.util.pmcTable
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream

class CommandProposeImportBlockchain : DCBaseCommand(
        name = "import",
        help = """
            Propose importing a blockchain in a specific container 
            
            Change will be applied after voting within the deployer voter set 
            of the cluster that the container belongs to.
        """.trimIndent(),
        requiresVersion = 19
) {
    private val configurationsFile by configurationsFileOption().required()

    private val container by option("-c", "--container", help = "Name of container to run in").required()

    private val name by nameOption("Name of blockchain").required().validate(entityNameValidator())

    private val description by nullableProposalDescriptionOption()

    private fun description() = description ?: run {
        "Import blockchain $name with blockchain-rid: $blockchainRid from a file to the container $container"
    }

    private lateinit var blockchainRid: BlockchainRid

    private var preloadedConfig: Gtv? = null

    override fun runDC() {
        if (dcVersion <= 53) {
            val containerInfo = client.getContainerData(container)
            val vsInfo = client.getVoterSetInfo(containerInfo.deployer)
            if (vsInfo.members.size > 1 && vsInfo.threshold != 1L) {
                throw CliktError("Directory chain version $dcVersion only allows importing the blockchain without voting. " +
                        "Please set the container deployer threshold value to 1 or update the Directory chain to version 54 or higher.")
            }
            runImplV53()
        } else {
            runImpl()
        }
    }

    @Suppress("DuplicatedCode")
    private fun runImpl() {
        BufferedInputStream(FileInputStream(configurationsFile.toFile())).use {
            blockchainRid = BlockchainRid(GtvDecoder.decodeGtv(it).asByteArray())

            // Load initial config
            val initialConfigData = GtvDecoder.decodeGtv(it).run {
                require(asArray()[0].asInteger() == 0L)
                asArray()[1].asByteArray()
            }

            // Preload the 2nd config
            preloadNextConfig(it)

            // Check if the blockchain already exists
            val bcInfo = client.getBlockchainInfo(blockchainRid.data)
            if (bcInfo == null) {
                client.getBlockchainImportProposalId(blockchainRid)?.let { rowid ->
                    echo("Import of blockchain $name with RID $blockchainRid is already proposed. Let other providers vote for the proposal [${rowid.id}], then repeat this command to import blockchain configurations.")
                    return
                }

                echo("Blockchain $name with RID $blockchainRid will be imported")
                val txResult = proposeImportBlockchain(blockchainRid, initialConfigData)

                val proposalId = client.getProposalIdsByTxRid(txResult.txRid.rid.hexStringToByteArray()).firstOrNull()
                        ?: throw CliktError("Can't find proposal by transaction RID: ${txResult.txRid}")
                val proposal = client.getProposal(proposalId)
                        ?: throw CliktError("Can't find proposal by proposal ID: $proposalId")
                if (proposal.state == ProposalState.PENDING) {
                    if (preloadedConfig != null && !preloadedConfig!!.isNull()) {
                        echo("Let other providers vote for the proposal [${proposal.id.id}] and repeat this command to import blockchain configurations.")
                    } else {
                        echo("Let other providers vote for the proposal [${proposal.id.id}] and move on to importing the blocks.")
                    }
                    return
                }
            } else {
                if (bcInfo.state != BlockchainState.IMPORTING) {
                    throw CliktError("Blockchain must be in IMPORTING state to import configurations")
                }
            }

            // Process configs
            var txBuilder = newTxBuilder()
            var numConfigs = 1
            val txs = buildList {
                while (true) {
                    val gtv = loadNextConfig(it)
                    if (gtv.isNull()) {
                        break
                    }
                    val height = gtv.asArray()[0].asInteger()
                    require(height > 0)
                    val configData = gtv.asArray()[1].asByteArray()
                    echo("Importing configuration at height $height...")
                    val wasAdded = txBuilder.tryAddConfiguration(blockchainRid, height, configData)
                    if (!wasAdded) {
                        postTransaction(txBuilder)?.also(::add)
                        txBuilder = newTxBuilder()
                        if (!txBuilder.tryAddConfiguration(blockchainRid, height, configData)) {
                            throw CliktError("Configuration does not fit in new transaction")
                        }
                    }
                    numConfigs++
                }
                postTransaction(txBuilder)?.also(::add)
            }

            val proposals = mutableListOf<ProposalData?>()
            txs.forEach { tx ->
                client.awaitConfirmation(tx, client.config.statusPollCount, client.config.statusPollInterval).also { result ->
                    result.printResult(
                            "Transaction ${tx.rid} confirmed",
                            "Cannot import blockchain config(s): ${result.rejectReason}", true
                    )
                }

                val txProposals = client.getProposalIdsByTxRid(tx.rid.hexStringToByteArray())
                txProposals.forEach { proposalId ->
                    proposals.add(client.getProposal(proposalId))
                }
            }

            if (proposals.filterNotNull().any { proposal -> proposal.state == ProposalState.PENDING }) {
                echo("$numConfigs blockchain configuration import(s) proposed for blockchain $name with RID $blockchainRid")
                echo("Let other providers vote for the proposals and move on to importing the blocks:")

                echo(pmcTable(
                        "proposals",
                        listOf("Type", "Id", "State"),
                        proposals.filterNotNull().map { info ->
                            listOf(info.type.toString(), info.id.id.toString(), info.state.toString())
                        },
                        null,
                        terminal.terminalInfo.inputInteractive
                ))

            } else {
                echo("$numConfigs blockchain configuration(s) imported for blockchain $name with RID $blockchainRid")
                echo("Move on to importing the blocks")
            }
        }
    }

    @Suppress("DuplicatedCode")
    private fun runImplV53() {
        BufferedInputStream(FileInputStream(configurationsFile.toFile())).use {
            blockchainRid = BlockchainRid(GtvDecoder.decodeGtv(it).asByteArray())

            // Load initial config
            val initialConfigData = GtvDecoder.decodeGtv(it).run {
                require(asArray()[0].asInteger() == 0L)
                asArray()[1].asByteArray()
            }

            // Preload the 2nd config
            preloadNextConfig(it)

            // Check if the blockchain already exists
            val bcInfo = client.getBlockchainInfo(blockchainRid.data)
            if (bcInfo == null) {
                echo("Blockchain $name with RID $blockchainRid will be imported")
                proposeImportBlockchain(blockchainRid, initialConfigData)
                val bcInfo0 = client.getBlockchainInfo(blockchainRid.data)
                if (bcInfo0 == null) {
                    if (preloadedConfig != null && !preloadedConfig!!.isNull()) {
                        echo("Let other providers vote for the proposal and repeat this command to import blockchain configurations.")
                    } else {
                        echo("Let other providers vote for the proposal and move on to importing the blocks.")
                    }
                    return
                }
            } else {
                if (bcInfo.state != BlockchainState.IMPORTING) {
                    throw CliktError("Blockchain must be in IMPORTING state to import configurations")
                }
            }

            // Process configs
            var txBuilder = newTxBuilder()
            var numConfigs = 1
            val txs = buildList {
                while (true) {
                    val gtv = loadNextConfig(it)
                    if (gtv.isNull()) {
                        break
                    }
                    val height = gtv.asArray()[0].asInteger()
                    require(height > 0)
                    val configData = gtv.asArray()[1].asByteArray()
                    val wasAdded = txBuilder.tryAddConfiguration(blockchainRid, height, configData)
                    if (!wasAdded) {
                        postTransaction(txBuilder)?.also(::add)
                        txBuilder = newTxBuilder()
                        if (!txBuilder.tryAddConfiguration(blockchainRid, height, configData)) {
                            throw CliktError("Configuration does not fit in new transaction")
                        }
                    }
                    numConfigs++
                }
                postTransaction(txBuilder)?.also(::add)
            }

            txs.forEach { tx ->
                client.awaitConfirmation(tx, client.config.statusPollCount, client.config.statusPollInterval).also { result ->
                    result.printResult(
                            "Transaction ${tx.rid} confirmed",
                            "Cannot import blockchain config(s): ${result.rejectReason}", true
                    )
                }
            }

            echo("$numConfigs blockchain configuration(s) imported for blockchain $name with RID $blockchainRid")
            echo("Move on to importing the blocks")
        }
    }

    private fun proposeImportBlockchain(blockchainRid: BlockchainRid, initialConfigData: ByteArray): TransactionResult {
        return client.transactionBuilder()
                .proposeImportBlockchainOperation(clientProviderPubkey, initialConfigData, blockchainRid, name, container, description())
                .postAwaitConfirmation(txListener())
                .also {
                    it.printResult(
                            "Blockchain import has been proposed",
                            "Cannot import blockchain: ${it.rejectReason}", true)
                }
    }

    private fun TrackingTransactionBuilder.tryAddConfiguration(blockchainRid: BlockchainRid, height: Long, configData: ByteArray) =
            try {
                txBuilder.proposeImportConfigurationOperation(
                        clientProviderPubkey, blockchainRid, height, configData, "Import blockchain configuration from a file - blockchain-rid: $blockchainRid, height: $height")
                opCounter++
                true
            } catch (_: IllegalStateException) {
                false
            }

    private fun postTransaction(trackingTxBuilder: TrackingTransactionBuilder): TxRid? {
        if (trackingTxBuilder.opCounter == 0) return null

        return trackingTxBuilder.txBuilder.post()
                .also { result ->
                    if (result.status == TransactionStatus.REJECTED) {
                        throw CliktError("Cannot import blockchain config(s): ${result.rejectReason}")
                    }
                    echo("Transaction ${result.txRid} submitted")
                }.txRid
    }

    private class TrackingTransactionBuilder(
            val txBuilder: TransactionBuilder,
            var opCounter: Int = 0
    )

    private fun newTxBuilder() = TrackingTransactionBuilder(client.transactionBuilder())

    private fun preloadNextConfig(inputStream: InputStream) {
        require(preloadedConfig == null)
        preloadedConfig = GtvDecoder.decodeGtv(inputStream)
    }

    private fun readPreloadedConfig(): Gtv? {
        val cfg = preloadedConfig
        preloadedConfig = null
        return cfg
    }

    private fun loadNextConfig(inputStream: InputStream): Gtv {
        return readPreloadedConfig() ?: GtvDecoder.decodeGtv(inputStream)
    }
}
