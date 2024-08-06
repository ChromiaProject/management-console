package net.postchain.mc.cli.blockchain.import_chain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.deprecated
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.proposal_blockchain_import.proposeForeignBlockchainBlocksImportOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion

class CommandProposeImportForeignBlocks : CliktCommand(
        name = "import-foreign-blocks",
        help = """
            Propose importing a foreign blockchain blocks 

            Change will be applied after voting within the deployer voter set 
            of the cluster that the container belongs to.
        """.trimIndent()
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val upToHeight by option("--up-to-height", help = "Import blocks up to and including this height")
            .deprecated("Use --final-height option")

    private val finalHeight by option("--final-height", help = "Import blocks up to and including this height")
            .long().required().validate {
                require(it > 0) { "--final-height arg must be greater than 0" }
            }

    private val description by proposalDescriptionOption {
        "Import foreign blockchain blocks - blockchain-rid: $blockchainRID, final-height: $finalHeight"
    }

    override fun run() {
        client.requireApiVersion(19)
        echo("Import of blocks of foreign blockchain ${blockchainRID.toHex()} will be proposed")
        val txBuilder = client.transactionBuilder()
        txBuilder.proposeForeignBlockchainBlocksImportOperation(client.pubkey, blockchainRID, finalHeight, description)
        txBuilder.postAwaitConfirmation()
                .printResult(
                        "Import of blocks of foreign blockchain ${blockchainRID.toHex()} proposed",
                        "Cannot propose import of blocks of foreign blockchain",
                        true
                )
    }
}
