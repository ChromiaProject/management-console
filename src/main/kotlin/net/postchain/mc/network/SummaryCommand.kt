package net.postchain.mc.network

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.rendering.TextAlign
import net.postchain.chain0.common.queries.getSummary
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class SummaryCommand : PmcCommand(
        help = "Show summary of the network"
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    override fun run() {
        val summary = client.getSummary()
        echo(pmcTable {
            captionTop("Network Summary", TextAlign.LEFT)
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
