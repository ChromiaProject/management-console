package net.postchain.mc.cli.container

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.direct_container.removeContainerOperation
import net.postchain.chain0.features.hasDirectContainer
import net.postchain.chain0.proposal_container.proposeRemoveContainerOperation
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption


class CommandProposeRemoveContainer : DCBaseCommand(
        name = "remove",
        help = "Propose removal of container. Command is irreversible"
) {
    private val name by nameOption("Container name to remove").required()

    private val description by proposalDescriptionOption { "Remove container $name" }

    private val direct by option("-d", "--direct", help = "Remove directly without proposal")
            .flag("-p", "--proposal", default = false)

    override fun runDC() {
        if (direct) {
            val apiVersion = client.apiVersion()
            // Before version 53 this operation was not part of "direct" container module
            if (apiVersion >= 53 && !directContainerEnabled(apiVersion)) {
                throw CliktError("Network does not support direct removal of containers. Create a proposal instead.")
            }
            transactionBuilder()
                    .removeContainerOperation(clientProviderPubkey, name)
                    .postOrSave()
                    .printResult(
                            "Container removed",
                            "Failed to remove container"
                    )
        } else {
            transactionBuilder()
                    .proposeRemoveContainerOperation(clientProviderPubkey, name, description)
                    .postOrSave()
                    .printResult(
                            "Container removal proposed",
                            "Failed proposing container removal"
                    )
        }
    }

    private fun directContainerEnabled(apiVersion: Long) =
            if (apiVersion >= 49) client.hasDirectContainer() else true
}