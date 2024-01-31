package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.economy.economy_chain.removeTagOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption

class CommandRemoveTag  : CliktCommand(
    name = "remove-tag",
    help = "Remove tag"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOption("Name of the tag to be removed").required()

    override fun run() {
        val economyChainClient = getEconomyChainClient(client, config.config)

        economyChainClient.transactionBuilder()
                .removeTagOperation(name)
                .postAwaitConfirmation()
                .printResult(
                        "Tag $name has been removed",
                        "Failed to remove tag"
                )
    }
}