package net.postchain.mc.cli.container

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_container.resumeContainerOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption

class CommandResumeContainer : DCBaseCommand(
        name = "resume",
        help = "Resume a container.",
        requiresVersion = 94,
) {
    private val containerName by nameOption("Container name").required()

    override fun runDC() {
        client.transactionBuilder()
                .resumeContainerOperation(
                                clientProviderPubkey,
                                containerName
                        )
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Container resumed successfully",
                        "Cannot resume container"
                )
    }
}
