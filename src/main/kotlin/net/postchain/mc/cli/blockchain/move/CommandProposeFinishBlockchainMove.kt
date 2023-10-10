package net.postchain.mc.cli.blockchain.move

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.proposal_blockchain_move.proposeBlockchainMoveFinishOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion

class CommandProposeFinishBlockchainMove : CliktCommand(
        name = "finish-moving",
        help = """
            Propose finishing of the blockchain moving
            
            Change will be applied after voting within the deployer voter set 
            of the cluster that the original container belongs to.
        """.trimIndent()
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val finishAtHeight by option("--finish-at-height", help = "Finish blockchain moving at height")
            .long().required().validate {
                require(it > 0) { "--finish-at-height arg must be greater than 0" }
            }

    private val description by proposalDescriptionOption(default = "Propose finishing of the blockchain moving")

    override fun run() {
        client.requireApiVersion(21)
        echo("Blockchain moving will be finished as soon as the proposal is approved")

        client.transactionBuilder()
                .proposeBlockchainMoveFinishOperation(client.pubkey, blockchainRID, finishAtHeight, description)
                .postAwaitConfirmation()
                .printResult(
                        "Finishing of the blockchain moving proposed",
                        "Cannot propose finishing of the blockchain moving"
                )
    }
}
