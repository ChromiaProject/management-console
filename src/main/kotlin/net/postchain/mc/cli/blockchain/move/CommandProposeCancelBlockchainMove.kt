package net.postchain.mc.cli.blockchain.move

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain_move.proposeBlockchainMoveCancelOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeCancelBlockchainMove : DCBaseCommand(
        name = "cancel-move",
        help = """
            Propose canceling the blockchain move
            
            Change will be applied after voting within the deployer voter set 
            of the cluster that the original container belongs to.
        """.trimIndent(),
        requiresVersion = 81
) {
    private val blockchainRID by blockchainRidOption().required()

    private val description by proposalDescriptionOption { "Cancel the blockchain move for $blockchainRID" }

    override fun runDC() {
        echo("Blockchain move will be canceled as soon as the proposal is approved")

        transactionBuilder()
                .proposeBlockchainMoveCancelOperation(clientProviderPubkey, blockchainRID, description)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Canceling the blockchain move has been proposed",
                        "Cannot propose canceling the blockchain move"
                )
    }
}
