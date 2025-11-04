package net.postchain.mc.cli.container

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_container.proposeContainerSubnodeJarExtensionOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeContainerSubnodeJarExtension : DCBaseCommand(
        name = "subnode-jar-extension",
        help = "Propose assigning subnode JAR extension to a container",
        requiresVersion = 102
) {
    private val containerName by nameOption("Container name").required()

    private val subnodeJarExtensionName by option("-sje", "--subnode-jar-extension-name", help = "Subnode JAR extension name").required()

    private val description by proposalDescriptionOption {
        "Assign subnode JAR extension $subnodeJarExtensionName to $containerName"
    }

    override fun runDC() {

        transactionBuilder()
                .proposeContainerSubnodeJarExtensionOperation(clientProviderPubkey, containerName, subnodeJarExtensionName, description)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Container subnode JAR extension proposed",
                        "Failed proposing container subnode JAR extension")
    }
}
