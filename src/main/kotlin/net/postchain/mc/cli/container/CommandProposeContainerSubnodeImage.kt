package net.postchain.mc.cli.container

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_container.proposeContainerSubnodeImageOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeContainerSubnodeImage : DCBaseCommand(
        name = "subnode-image",
        help = "Propose assigning subnode image to a container",
        requiresVersion = 62
) {
    private val containerName by nameOption("Container name").required()

    private val subnodeImageName by option("-sin", "--subnode-image-name", help = "Subnode image name").required()

    private val description by proposalDescriptionOption {
        "Assign subnode image $subnodeImageName to $containerName"
    }

    override fun runDC() {

        transactionBuilder()
                .proposeContainerSubnodeImageOperation(clientProviderPubkey, containerName, subnodeImageName, description)
                .postOrSave()
                .printResult(
                        "Container subnode image proposed",
                        "Failed proposing container subnode image")
    }
}
