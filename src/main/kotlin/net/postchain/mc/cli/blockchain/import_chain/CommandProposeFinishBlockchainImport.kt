package net.postchain.mc.cli.blockchain.import_chain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.options.convert
import net.postchain.chain0.proposal_blockchain_import.proposeFinishImportBlockchainOperation
import net.postchain.common.BlockchainRid
import net.postchain.gtv.GtvDecoder
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

    private val description by proposalDescriptionOption(default = "Propose finishing import of blockchain")

    override fun run() {
        client.requireApiVersion(5)
        echo("Import of blockchain ${blockchainRID.toHex()} will be finished")
        val txBuilder = client.transactionBuilder()
        txBuilder.proposeFinishImportBlockchainOperation(client.pubkey, blockchainRID, description)
        txBuilder.postAwaitConfirmation().printResult(
                "Import of blockchain ${blockchainRID.toHex()} finished",
                "Cannot finish blockchain import", true
        )
    }
}
