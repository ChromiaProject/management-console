package net.postchain.mc.cli.container

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.operations.removeContainerOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption


class CommandProposeRemoveContainer : CliktCommand(
        name = "remove",
        help = "Propose removal of container. Command is irreversible"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOption("Container name to remove").required()

    override fun run() {
        client.transactionBuilder()
                .removeContainerOperation(client.config.pubkey().data, name)
                .postAwaitConfirmation()
                .printResult(
                        "Container removal proposed",
                        "Failed proposing container removal"
                )
    }
}