package net.postchain.mc.cli.blockchain.move

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.proposal_blockchain_move.proposeBlockchainMoveFinishOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeFinishBlockchainMove : DCBaseCommand(
        name = "finish-move",
        help = """
            Propose finishing the blockchain move

            Change will be applied after voting within the deployer voter set 
            of the cluster that the original container belongs to.

            Note: as soon as the blockchain move is finalized, it will no longer be possible to cancel it.

        """.trimIndent(),
        requiresVersion = 33
) {
    private val blockchainRID by blockchainRidOption().required()

    private val finalHeight by option("--final-height", help = "Finish blockchain moving at height")
            .long().required().validate {
                require(it > 0) { "--final-height arg must be greater than 0" }
            }

    private val description by proposalDescriptionOption { "Finish the blockchain move - blockchain-rid: $blockchainRID, final-height: $finalHeight" }

    override fun runDC() {
        echo("Blockchain move will be finished as soon as the proposal is approved")

        transactionBuilder()
                .proposeBlockchainMoveFinishOperation(clientProviderPubkey, blockchainRID, finalHeight, description)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Finishing the blockchain move has been proposed",
                        "Cannot propose finishing the blockchain move"
                )
    }
}
