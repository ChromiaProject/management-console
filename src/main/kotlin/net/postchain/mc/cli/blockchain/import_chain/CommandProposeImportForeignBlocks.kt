package net.postchain.mc.cli.blockchain.import_chain

import com.github.ajalt.clikt.parameters.options.deprecated
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.proposal_blockchain_import.proposeForeignBlockchainBlocksImportOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeImportForeignBlocks : DCBaseCommand(
        name = "import-foreign-blocks",
        help = """
            Propose importing a foreign blockchain blocks 

            Change will be applied after voting within the deployer voter set 
            of the cluster that the container belongs to.
        """.trimIndent(),
        requiresVersion = 19
) {
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

    override fun runDC() {
        echo("Import of blocks of foreign blockchain ${blockchainRID.toHex()} will be proposed", err = true)
        val txBuilder = client.transactionBuilder()
        txBuilder.proposeForeignBlockchainBlocksImportOperation(clientProviderPubkey, blockchainRID, finalHeight, description)
        txBuilder.postAwaitConfirmation()
                .printResult(
                        "Import of blocks of foreign blockchain ${blockchainRID.toHex()} proposed",
                        "Cannot propose import of blocks of foreign blockchain",
                        true
                )
    }
}
