package net.postchain.mc.cli.economy

import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.getTags
import net.postchain.mc.cli.util.pmcTable

class CommandListTags : ECBaseCommand(
        name = "list-tags",
        help = "List all existing tags"
) {
    private val headers = listOf("Name", "SCU price", "Extra storage price")

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        val rows = economyChainClient.getTags()
                .map { listOf(it.name, formatUsd(it.scuPrice, ecVersion), formatUsd(it.extraStoragePrice, ecVersion)) }
        echo(pmcTable(
                "tags",
                headers,
                rows
        ))
    }
}