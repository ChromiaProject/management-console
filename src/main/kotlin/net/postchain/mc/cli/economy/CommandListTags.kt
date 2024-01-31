package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.economy.economy_chain.getTags
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandListTags : CliktCommand(
        name = "list-tags",
        help = "List all existing tags"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val headers = listOf("Name", "SCU price", "Extra storage price")

    override fun run() {
        val economyChainClient = getEconomyChainClient(client, config.config)
        val rows = economyChainClient.getTags()
                .map { listOf(it.name, it.scuPrice.toString(), it.extraStoragePrice.toString()) }
        echo(pmcTable(
                "tags",
                headers,
                rows
        ))
    }
}