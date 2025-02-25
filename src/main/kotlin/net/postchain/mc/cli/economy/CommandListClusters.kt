package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.input.interactiveSelectList
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.apiVersion
import net.postchain.economy.economy_chain.getClusters
import net.postchain.mc.cli.ECBaseCommand
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
        if (interactive && clusters.isNotEmpty() && clusters.size < terminal.size.height) {
            terminal.interactiveSelectList(clusters.map {
                it.name
            }, "Select cluster")?.let {
                showClusterInfo(client.apiVersion(), client, it)
            }
        } else {
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
}