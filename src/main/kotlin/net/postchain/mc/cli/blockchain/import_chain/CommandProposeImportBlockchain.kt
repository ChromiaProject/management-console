package net.postchain.mc.cli.blockchain.import_chain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.proposal_blockchain_import.proposeImportBlockchainOperation
import net.postchain.chain0.proposal_blockchain_import.proposeImportConfigurationOperation
import net.postchain.client.core.TxRid
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.BlockchainRid
import net.postchain.common.tx.TransactionStatus
import net.postchain.gtv.GtvDecoder
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.configurationsFileOption
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion

import java.io.BufferedInputStream
import java.io.FileInputStream

class CommandProposeImportBlockchain : CliktCommand(
        name = "import",
        help = """
            Propose importing a blockchain in a specific container 
            
            Change will be applied after voting within the deployer voter set 
            of the cluster that the container belongs to.
        """.trimIndent()
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val configurationsFile by configurationsFileOption().required()

    private val container by option("-c", "--container", help = "Name of container to run in").required()

    private val name by nameOption("Name of blockchain").required().validate(entityNameValidator())

    private val description by proposalDescriptionOption(default = "Propose importing of blockchain")

    override fun run() {
        client.requireApiVersion(19)
        BufferedInputStream(FileInputStream(configurationsFile.toFile())).use {
            val blockchainRid = BlockchainRid(GtvDecoder.decodeGtv(it).asByteArray())
            val initialConfig = GtvDecoder.decodeGtv(it)
            require(initialConfig.asArray()[0].asInteger() == 0L)
            val initialConfigData = initialConfig.asArray()[1].asByteArray()

            echo("Blockchain $name with bc-rid $blockchainRid will be imported")
            proposeImportBlockchain(blockchainRid, initialConfigData)

            var txBuilder = newTxBuilder()
            var numConfigs = 1
            val txs = buildList {
                while (true) {
                    val gtv = GtvDecoder.decodeGtv(it)
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
            echo("Blockchain $name with bc-rid $blockchainRid with $numConfigs blockchain configuration(s) imported")
        }
    }

    private fun proposeImportBlockchain(blockchainRid: BlockchainRid, initialConfigData: ByteArray) {
        client.transactionBuilder()
                .proposeImportBlockchainOperation(client.pubkey, initialConfigData, blockchainRid, name, container, description)
                .postAwaitConfirmation()
                .also {
                    it.printResult(
                            "Blockchain import started",
                            "Cannot import blockchain config(s): ${it.rejectReason}", true)
                }
    }

    private fun TrackingTransactionBuilder.tryAddConfiguration(blockchainRid: BlockchainRid, height: Long, configData: ByteArray) =
            try {
                txBuilder.proposeImportConfigurationOperation(client.pubkey, blockchainRid, height, configData, description)
                opCounter++
                true
            } catch (e: IllegalStateException) {
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
}
