package net.postchain.mc.cli.container

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_container.ContainerAction
import net.postchain.chain0.proposal_container.proposeContainerActionOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption

class CommandProposePauseContainer : DCBaseCommand(
        name = "pause",
        help = "Pause a container.",
        requiresVersion = 94,
) {
    private val containerName by nameOption("Container name").required()

    override fun runDC() {
        transactionBuilder()
                .proposeContainerActionOperation(
                                clientProviderPubkey,
                                containerName,
                                ContainerAction.pause,
                                "Pause container $containerName"
                        )
                .postOrSave()
                .printResult(
                        "Container paused proposition was added successfully",
                        "Cannot add proposal for pausing container",
                )
    }
}
