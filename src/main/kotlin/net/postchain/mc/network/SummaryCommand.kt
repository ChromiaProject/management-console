package net.postchain.mc.network

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.table.Borders
import net.postchain.chain0.common.queries.getSummary
import net.postchain.mc.cli.util.pmcConfigOption


class SummaryCommand : CliktCommand(
        help = "Show summary of the network"
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    override fun run() {
        val summary = client.getSummary()
        echo(defaultTable {
            header {
                row {
                    this.cellBorders = Borders.NONE
                    cell("Network Summary")
                }
            }
            body {
                row("Voter sets", summary.voterSets)
                row("Providers", summary.providers)
                row("Clusters", summary.clusters)
                row("Containers", summary.containers)
                row("Nodes", summary.nodes)
                row("Blockchains", summary.blockchains)
            }
        })
    }
}
