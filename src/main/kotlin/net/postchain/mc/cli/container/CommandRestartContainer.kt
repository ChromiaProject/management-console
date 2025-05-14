    package net.postchain.mc.cli.container

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getContainerBlockchain
import net.postchain.chain0.model.BlockchainState
import net.postchain.chain0.proposal_container.ContainerAction
import net.postchain.chain0.proposal_container.proposeContainerActionOperation
import net.postchain.chain0.proposal_container.resumeContainerOperation
import net.postchain.client.core.PostchainClient
import net.postchain.client.exception.ClientError
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption

class CommandRestartContainer : DCBaseCommand(
        name = "restart",
        help = "Restart a container by pausing it, waiting for it to be paused and then resuming it.",
        requiresVersion = 94,
) {
    private val containerName by nameOption("Container name").required()

    override fun runDC() {
        val blockchains = client.getContainerBlockchain(containerName)
        val blockchainRid = blockchains.firstOrNull { it.state == BlockchainState.RUNNING }?.let { BlockchainRid(it.rid) }
                ?: throw CliktError("Cannot restart a container without running blockchains")
        val blockchainClient = config.chromiaClient.getClient(blockchainRid)
        try {
            blockchainClient.currentBlockHeight(containerName)
        } catch (e: ClientError) {
            throw CliktError("Cannot restart a container without a working blockchain: ${e.message}")
        }

        client.transactionBuilder()
                .proposeContainerActionOperation(
                        clientProviderPubkey,
                        containerName,
                        ContainerAction.pause,
                        "Pause container $containerName"
                )
                .postAwaitConfirmation()
                .printResult(
                        "Container paused proposition was added successfully",
                        "Cannot add proposal for pausing container",
                        printOnSuccess = true,
                )

        waitForContainerToPause(blockchainClient)

        client.transactionBuilder()
                .resumeContainerOperation(
                        clientProviderPubkey,
                        containerName
                )
                .postAwaitConfirmation()
                .printResult(
                        "Container resumed successfully",
                        "Cannot resume container"
                )
    }

    private fun waitForContainerToPause(blockchainClient: PostchainClient) {
        echo("Waiting 60 seconds for container to pause...", trailingNewline = false)
        repeat(30) {
            try {
                blockchainClient.currentBlockHeight(containerName)
                echo(".", trailingNewline = false)
            } catch (_: ClientError) {
                echo("done")
                return
            }
            Thread.sleep(2 * 1000)
        }
        echo("timeout!")
        throw CliktError("Container did not pause after 60 seconds, run 'pmc container resume' to manually resume the container later.")
    }
}
