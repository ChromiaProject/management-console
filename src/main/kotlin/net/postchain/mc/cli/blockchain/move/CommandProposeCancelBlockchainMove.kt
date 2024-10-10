package net.postchain.mc.cli.blockchain.move

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeCancelBlockchainMove : PmcCommand(
        name = "cancel-moving",
        help = """
            Propose canceling of the blockchain moving
            
            Change will be applied after voting within the deployer voter set 
            of the cluster that the original container belongs to.
        """.trimIndent()
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val description by proposalDescriptionOption { "Cancel blockchain moving for $blockchainRID" }

    override fun run() {
        throw PrintMessage("Not yet implemented")

        /*
        client.requireApiVersion(21)
        echo("Blockchain moving will be canceled as soon as the proposal is approved")

        client.transactionBuilder()
                .proposeBlockchainMoveCancelOperation(client.pubkey, blockchainRID, description)
                .postAwaitConfirmation()
                .printResult(
                        "Canceling of the blockchain moving proposed",
                        "Cannot propose canceling of the blockchain moving"
                )

         */
    }
}
