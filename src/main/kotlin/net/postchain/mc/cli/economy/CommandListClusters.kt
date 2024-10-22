package net.postchain.mc.cli.economy

import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.apiVersion
import net.postchain.economy.economy_chain.getClusters
import net.postchain.mc.cli.cluster.showClusterInfo
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcTable

class CommandListClusters : ECBaseCommand(
        name = "list-clusters",
        help = "List all clusters that are tracked by economy chain",
        printHelpOnEmptyArgs = false
) {
    private val interactive by interactiveOption()

    private val headers = listOf("Name", "Tag")

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        val clusters = economyChainClient.getClusters()
        echo(pmcTable(
                "clusters",
                headers,
                clusters.map { listOf(it.name, it.tagName) },
                interactive = interactive
        ))
        if (interactive && clusters.isNotEmpty()) {
            promptForIndex(clusters)?.let {
                showClusterInfo(client.apiVersion(), client, clusters[it].name)
            }
        }
    }
}