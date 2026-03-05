package net.postchain.mc.cli.blockchain.move

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.mordant.terminal.prompt
import net.postchain.chain0.common.queries.getBlockchainInfo
import net.postchain.chain0.model.BlockchainState
import net.postchain.chain0.proposal_blockchain_move.PROPOSE_BLOCKCHAIN_MOVE
import net.postchain.chain0.proposal_blockchain_move.proposeBlockchainMoveOperation
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeBlockchainMove : DCBaseCommand(
        name = "move",
        help = """
            Propose moving a blockchain to a specific container

            When approved, destination cluster nodes are added as replicas to sync.
            The blockchain stays in the source container until the move is finalized
            (see finish-move). Change will be applied after voting within the deployer
            voter set of the cluster that the original container belongs to.
        """.trimIndent(),
        requiresVersion = 33
) {
    private val blockchainRID by blockchainRidOption().required()

    private val destinationContainer by option("-dc", "--destination-container", help = "Name of container to move blockchain to").required()

    private val description by proposalDescriptionOption { "Move blockchain $blockchainRID to the container $destinationContainer" }

    private val yes by option("-y", "--yes", help = "Skip confirmation prompt for paused blockchains").flag()

    private val keepSrcReplica by option(
            "--keep-src-replica",
            help = "After the move is finalized, keep source cluster nodes as replicas. Requires DC version 110 or above. (default: true)"
    ).flag("--no-keep-src-replica", default = true)

    override fun runDC() {
        if (terminal.terminalInfo.inputInteractive && !yes) {
            val blockchainInfo = client.getBlockchainInfo(blockchainRID.data) ?: throw CliktError("Blockchain not found")
            if (blockchainInfo.state == BlockchainState.PAUSED) {
                val answer = terminal.prompt("WARNING: Blockchain is ${BlockchainState.PAUSED}. Move may take time and blockchain cannot be resumed until the move is finished. Continue? (y/N)")
                if (answer == null || !answer.startsWith("Y", ignoreCase = true))
                    throw CliktError("Canceled")
            }
        }

        echo("Destination cluster nodes will begin syncing blockchain $blockchainRID to container $destinationContainer once the proposal is approved")

        transactionBuilder()
                .apply {
                    if (dcVersion >= 110L) {
                        proposeBlockchainMoveOperation(clientProviderPubkey, blockchainRID, destinationContainer, description, keepSrcReplica)
                    } else {
                        addOperation(PROPOSE_BLOCKCHAIN_MOVE, gtv(clientProviderPubkey), gtv(blockchainRID), gtv(destinationContainer), gtv(description))
                    }
                }
                .postOrSave()
                .printResult(
                        "Blockchain move proposed",
                        "Cannot propose moving the blockchain"
                )
    }
}
