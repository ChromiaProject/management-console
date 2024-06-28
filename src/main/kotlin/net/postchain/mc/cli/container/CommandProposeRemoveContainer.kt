package net.postchain.mc.cli.container

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.direct_container.removeContainerOperation
import net.postchain.chain0.features.hasDirectContainer
import net.postchain.chain0.proposal_container.proposeRemoveContainerOperation
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption


class CommandProposeRemoveContainer : CliktCommand(
        name = "remove",
        help = "Propose removal of container. Command is irreversible"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOption("Container name to remove").required()

    private val description by proposalDescriptionOption()

    private val direct by option("-d", "--direct", help = "Remove directly without proposal")
            .flag("-p", "--proposal", default = false)

    override fun run() {
        if (direct) {
            val apiVersion = client.apiVersion()
            // Before version 53 this operation was not part of "direct" container module
            if (apiVersion >= 53 && !directContainerEnabled(apiVersion)) {
                throw CliktError("Network does not support direct removal of containers. Create a proposal instead.")
            }
            client.transactionBuilder()
                    .removeContainerOperation(client.config.pubkey().data, name)
                    .postAwaitConfirmation()
                    .printResult(
                            "Container removed",
                            "Failed to remove container"
                    )
        } else {
            client.transactionBuilder()
                    .proposeRemoveContainerOperation(client.config.pubkey().data, name, description)
                    .postAwaitConfirmation()
                    .printResult(
                            "Container removal proposed",
                            "Failed proposing container removal"
                    )
        }
    }

    private fun directContainerEnabled(apiVersion: Long) =
            if (apiVersion >= 49) client.hasDirectContainer() else true
}