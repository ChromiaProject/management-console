package net.postchain.mc.cli.blockchain.move

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain_move.proposeBlockchainMoveCancelOperation
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion

class CommandProposeCancelBlockchainMove : PmcCommand(
        name = "cancel-move",
        help = """
            Propose canceling the blockchain move
            
            Change will be applied after voting within the deployer voter set 
            of the cluster that the original container belongs to.
        """.trimIndent()
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val description by proposalDescriptionOption { "Cancel the blockchain move for $blockchainRID" }

    override fun run() {
        client.requireApiVersion(81)
        echo("Blockchain move will be canceled as soon as the proposal is approved")

        client.transactionBuilder()
                .proposeBlockchainMoveCancelOperation(client.pubkey, blockchainRID, description)
                .postAwaitConfirmation()
                .printResult(
                        "Canceling the blockchain move has been proposed",
                        "Cannot propose canceling the blockchain move"
                )
    }
}
