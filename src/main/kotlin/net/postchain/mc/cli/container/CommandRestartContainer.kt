package net.postchain.mc.cli.container

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getContainerBlockchain
import net.postchain.chain0.model.BlockchainState
import net.postchain.chain0.proposal_blockchain.BlockchainAction
import net.postchain.chain0.proposal_blockchain.proposeBlockchainActionOperation
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption

class CommandRestartContainer : DCBaseCommand(
        name = "restart",
        help = "Restart a container by pausing and resuming all blockchains in it.",
        requiresVersion = 4,
) {
    private val containerName by nameOption("Container name").required()

    override fun runDC() {
        val blockchains = client.getContainerBlockchain(containerName)
                .filter { it.state == BlockchainState.RUNNING }
                .map { BlockchainRid(it.rid) }

        if (blockchains.isEmpty()) {
            throw CliktError("No running blockchains found in container $containerName")
        }

        client.transactionBuilder()
                .apply {
                    for (bc in blockchains) {
                        proposeBlockchainActionOperation(
                                clientProviderPubkey,
                                bc,
                                BlockchainAction.pause,
                                "Pause blockchain $bc"
                        )
                    }
                }
                .postAwaitConfirmation()
                .printResult(
                        "Blockchains pause proposition was added successfully",
                        "Cannot add proposal for pausing blockchains",
                        printOnSuccess = true,
                )

        client.transactionBuilder()
                .apply {
                    for (bc in blockchains) {
                        proposeBlockchainActionOperation(
                                clientProviderPubkey,
                                bc,
                                BlockchainAction.resume,
                                "Resume blockchain $bc"
                        )
                    }
                }
                .postAwaitConfirmation()
                .printResult(
                        "Blockchains resume proposition was added successfully",
                        "Cannot add proposal for resuming blockchains"
                )
    }
}
