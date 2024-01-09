package net.postchain.mc.cli.cluster

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getClusters
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.base.NAME_LENGTH_MAX
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandListClusters : CliktCommand(
        name = "list",
        help = "List all existing clusters"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()

    private val headers = listOf("Name", "Governor", "Operational")

    override fun run() {
        val apiVersion = client.apiVersion()
        val clusters = client.getClusters()
        echo(pmcTable(
                "clusters",
                headers,
                clusters.map { listOf(it.name, it.governor, it.operational.toString()) },
                0 to NAME_LENGTH_MAX,
                interactive
        ))
        if (interactive && clusters.isNotEmpty()) {
            promptForIndex(clusters)?.let {
                showClusterInfo(apiVersion, client, clusters[it].name)
            }
        }
    }
}
