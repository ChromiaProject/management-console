package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.economy.economy_chain.apiVersion
import net.postchain.economy.economy_chain.getClusters
import net.postchain.mc.cli.cluster.showClusterInfo
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandListClusters : CliktCommand(
        name = "list-clusters",
        help = "List all clusters that are tracked by economy chain"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val interactive by interactiveOption()

    private val headers = listOf("Name", "Tag")

    override fun run() {
        val economyChainClient = getEconomyChainClient(client, config.config)
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