package net.postchain.mc.cli.cluster

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getClusterContainers
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.nameOption


class CommandListClusterContainers : CliktCommand(
        name = "containers",
        help = "List all existing cluster containers"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val clusterName by nameOption("Cluster name").required()

    override fun run() {
        val containers = client.getClusterContainers(clusterName)
        if (containers.isEmpty()) {
            echo("No containers")
        } else {
            echo(defaultTable {
                header { row("Container name", "deployer") }
                body {
                    containers.forEach {
                        row(it.name, it.deployer)
                    }
                }
            })
        }
    }
}