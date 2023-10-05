package net.postchain.mc.cli.blockchain.import_chain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.proposal_blockchain_import.proposeFinishImportBlockchainOperation
import net.postchain.chain0.version.apiVersion
import net.postchain.common.BlockchainRid
import net.postchain.gtv.GtvDecoder
import net.postchain.gtv.GtvFactory
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.configurationsFileOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion
import java.io.BufferedInputStream
import java.io.FileInputStream

class CommandProposeFinishBlockchainImport : CliktCommand(
        name = "finish-import",
        help = """
            Propose finishing import of a blockchain
            
            Change will be applied after voting within the deployer voter set 
            of the cluster that the container belongs to.
        """.trimIndent()
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by mutuallyExclusiveOptions(
            configurationsFileOption().convert { path ->
                BufferedInputStream(FileInputStream(path.toFile())).use {
                    BlockchainRid(GtvDecoder.decodeGtv(it).asByteArray())
                }
            },
            blockchainRidOption()
    ).required()

    private val finishAtHeight by option("--finish-at-height", help = "Finish blockchain import at height (required for API version 18 and later)").long()

    private val description by proposalDescriptionOption(default = "Propose finishing import of blockchain")

    override fun run() {
        client.requireApiVersion(5)
        echo("Import of blockchain ${blockchainRID.toHex()} will be finished")
        if (client.apiVersion() < 18) {
            echo("Make sure the ebft-majority of the cluster blockchain is being imported in is equal to the ebft-majority of the last used blockchain configuration")
            client.transactionBuilder()
                    .addOperation(
                            "propose_finish_import_blockchain",
                            GtvFactory.gtv(client.pubkey),
                            GtvFactory.gtv(blockchainRID),
                            GtvFactory.gtv(description)
                    )
                    .postAwaitConfirmation()
                    .printResult(
                            "Import of blockchain ${blockchainRID.toHex()} finished",
                            "Cannot finish blockchain import", true
                    )
        } else {
            val height = finishAtHeight
                    ?: throw UsageError("--finish-at-height is required for API version 18 and later")
            client.transactionBuilder()
                    .proposeFinishImportBlockchainOperation(client.pubkey, blockchainRID, height, description)
                    .postAwaitConfirmation()
                    .printResult(
                            "Import of blockchain ${blockchainRID.toHex()} finished",
                            "Cannot finish blockchain import", true
                    )
        }
    }
}
