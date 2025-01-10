package net.postchain.mc.cli.economy

import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.apiVersion
import net.postchain.economy.economy_chain.getClusters
import net.postchain.mc.cli.base.NAME_LENGTH_MAX
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

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        val clusters = economyChainClient.getClusters()
        echo(pmcTable(
                "clusters",
                listOf("Name", "Tag", "Operational", "Number of nodes", "Cluster Units", "Extra Storage"),
                clusters.map {
                    listOf(it.name, it.tagName, it.isOperational.toString(), it.numberOfNodes?.toString() ?: "",
                            it.clusterUnits?.toString() ?: "", it.extraStorage?.toString() ?: "")
                },
                0 to NAME_LENGTH_MAX,
                interactive = interactive
        ))
        if (interactive && clusters.isNotEmpty()) {
            promptForIndex(clusters)?.let {
                showClusterInfo(client.apiVersion(), client, clusters[it].name)
            }
        }
    }
}