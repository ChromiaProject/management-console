package net.postchain.mc.cli.cluster

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.table.ColumnWidth
import net.postchain.chain0.common.queries.getClusters
import net.postchain.mc.cli.base.NAME_LENGTH_MAX
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption

class CommandListClusters : CliktCommand(
        name = "list",
        help = "List all existing clusters"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()

    private val headers = listOf("Name", "Governor", "Operational")

    override fun run() {
        val clusters = client.getClusters()
        if (clusters.isEmpty()) {
            echo("No clusters")
        } else {
            echo(defaultTable {
                column(0) {
                    width = ColumnWidth.Fixed(if (interactive) 3 else NAME_LENGTH_MAX + 2)
                }
                header { rowFrom(if (interactive) listOf("#") + headers else headers) }
                body {
                    clusters.forEachIndexed { index, it ->
                        val columns = listOf(it.name, it.governor, it.operational.toString())
                        rowFrom(if (interactive) listOf(index.toString()) + columns else columns)
                    }
                }
            })
            if (interactive) {
                promptForIndex(clusters)?.let {
                    showClusterInfo(client, clusters[it].name)
                }
            }
        }
    }
}
