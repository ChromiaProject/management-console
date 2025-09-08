package net.postchain.mc.cli.blockchain.move

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain_move.proposeBlockchainMoveOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeBlockchainMove : DCBaseCommand(
        name = "move",
        help = """
            Propose moving a blockchain to a specific container 
            
            Change will be applied after voting within the deployer voter set 
            of the cluster that the original container belongs to.
        """.trimIndent(),
        requiresVersion = 33
) {
    private val blockchainRID by blockchainRidOption().required()

    private val destinationContainer by option("-dc", "--destination-container", help = "Name of container to move blockchain to").required()

    private val description by proposalDescriptionOption { "Move blockchain $blockchainRID to the container $destinationContainer" }

    override fun runDC() {
        echo("Blockchain $blockchainRID will start moving to container $destinationContainer as soon as the proposal is approved")

        transactionBuilder()
                .proposeBlockchainMoveOperation(clientProviderPubkey, blockchainRID, destinationContainer, description)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Blockchain move proposed",
                        "Cannot propose moving the blockchain"
                )
    }
}
