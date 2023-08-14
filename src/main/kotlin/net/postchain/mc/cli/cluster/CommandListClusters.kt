package net.postchain.mc.cli.cluster

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getClusters
import net.postchain.mc.cli.util.pmcConfigOption

class CommandListClusters : CliktCommand(
        name = "list",
        help = "List all existing clusters"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    override fun run() {
        val clusters = client.getClusters()
        if (clusters.isEmpty()) {
            echo("No clusters")
        } else {
            echo(defaultTable {
                header { row("Name", "Governor", "Operational") }
                body {
                    clusters.forEach {
                        row(it.name, it.governor, it.operational.toString())
                    }
                }
            })
        }
    }
}
