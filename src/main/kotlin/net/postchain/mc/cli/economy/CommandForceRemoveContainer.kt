package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain_remove_container.forceRemoveContainerOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption

class CommandForceRemoveContainer : ECBaseCommand(
        name = "force-remove-container",
        help = "Force removal of a container and its associated lease without refund",
        requiresECVersion = 47
) {
    override val hiddenFromHelp: Boolean
        get() = true

    private val name by nameOption("Name of container to remove").required()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        economyChainClient.transactionBuilder()
                .forceRemoveContainerOperation(name)
                .postAwaitConfirmation()
                .printResult(
                        "Container $name will be removed",
                        "Failed to remove container"
                )
    }
}
