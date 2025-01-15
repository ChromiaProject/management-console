package net.postchain.mc.cli.cluster

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.input.interactiveSelectList
import net.postchain.chain0.common.queries.getClusters
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.NAME_LENGTH_MAX
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV75.getClustersV75

class CommandListClusters : DCBaseCommand(
        name = "list",
        help = "List all existing clusters",
        printHelpOnEmptyArgs = false,
) {
    private val interactive by interactiveOption()

    override fun runDC() {
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
