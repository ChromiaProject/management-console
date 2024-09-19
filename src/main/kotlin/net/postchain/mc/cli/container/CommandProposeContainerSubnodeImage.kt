package net.postchain.mc.cli.container

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion

class CommandProposeContainerSubnodeImage : CliktCommand(
        name = "subnode-image",
        help = "Propose assigning subnode image to a container"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val containerName by nameOption("Container name").required()

    private val subnodeImageName by option("-sin", "--subnode-image-name", help = "Subnode image name").required()

    private val description by proposalDescriptionOption {
        "Assign subnode image $subnodeImageName to $containerName"
    }

    override fun run() {
        client.requireApiVersion(62)

        client.transactionBuilder()
                .proposeContainerSubnodeImageOperation(client.config.pubkey().data, containerName, subnodeImageName, description)
                .postAwaitConfirmation()
                .printResult(
                        "Container subnode image proposed",
                        "Failed proposing container subnode image")
    }
}
