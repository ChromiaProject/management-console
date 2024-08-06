package net.postchain.mc.cli.blockchain.move

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain_move.proposeBlockchainMoveOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion

class CommandProposeBlockchainMove : CliktCommand(
        name = "move",
        help = """
            Propose moving of blockchain to a specific container 
            
            Change will be applied after voting within the deployer voter set 
            of the cluster that the original container belongs to.
        """.trimIndent()
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val destinationContainer by option("-dc", "--destination-container", help = "Name of container to move blockchain to").required()

    private val description by proposalDescriptionOption { "Move blockchain $blockchainRID to the container $destinationContainer" }

    override fun run() {
        client.requireApiVersion(33)
        echo("Blockchain $blockchainRID will start moving to the container $destinationContainer as soon as the proposal approved")

        client.transactionBuilder()
                .proposeBlockchainMoveOperation(client.pubkey, blockchainRID, destinationContainer, description)
                .postAwaitConfirmation()
                .printResult(
                        "Moving of blockchain ${blockchainRID.toHex()} to the container $destinationContainer proposed",
                        "Cannot propose moving of blockchain",
                )
    }
}
