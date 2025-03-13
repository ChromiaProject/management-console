package net.postchain.mc.cli.cluster

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.input.interactiveSelectList
import net.postchain.chain0.common.queries.getClusters
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.base.NAME_LENGTH_MAX
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV75.getClustersV75
import net.postchain.mc.network.Version

class CommandListClusters : PmcCommand(
        name = "list",
        help = "List all existing clusters",
) {
    val config by pmcConfigOption()
    val client get() = config.client
    val dcVersion get() = Version(config.client).version
    private val interactive by interactiveOption()

    override fun run() {
        if (dcVersion >= 76) {
            val clusters = client.getClusters()
            if (interactive && clusters.isNotEmpty() && clusters.size < terminal.size.height) {
                terminal.interactiveSelectList(clusters.map {
                    it.name
                }, "Select cluster")?.let {
                    showClusterInfo(dcVersion, client, it)
                }
            } else {
                echo(pmcTable(
                        "clusters",
                        listOf("Name", "Governor", "Operational", "Number of nodes",
                                "Cluster Units", "Extra Storage",
                                "Container Units available", "Extra Storage available"),
                        clusters.map {
                            listOf(it.name, it.governor, it.isOperational.toString(), it.numberOfNodes?.toString()
                                    ?: "",
                                    it.clusterUnits?.toString() ?: "", it.extraStorage?.toString() ?: "",
                                    it.containerUnitsAvailable?.toString() ?: "", it.extraStorageAvailable?.toString()
                                    ?: "")
                        },
                        0 to NAME_LENGTH_MAX,
                        interactive
                ))
                if (interactive && clusters.isNotEmpty()) {
                    promptForIndex(clusters)?.let {
                        showClusterInfo(dcVersion, client, clusters[it].name)
                    }
                }
            }
        } else {
            val clusters = client.getClustersV75()
            if (interactive && clusters.isNotEmpty() && clusters.size < terminal.size.height) {
                terminal.interactiveSelectList(clusters.map {
                    it.name
                }, "Select cluster")?.let {
                    showClusterInfo(dcVersion, client, it)
                }
            } else {
                echo(pmcTable(
                        "clusters",
                        listOf("Name", "Governor", "Operational"),
                        clusters.map { listOf(it.name, it.governor, it.operational.toString()) },
                        0 to NAME_LENGTH_MAX,
                        interactive
                ))
                if (interactive && clusters.isNotEmpty()) {
                    promptForIndex(clusters)?.let {
                        showClusterInfo(dcVersion, client, clusters[it].name)
                    }
                }
            }
        }
    }
}
