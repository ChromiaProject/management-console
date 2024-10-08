package net.postchain.mc.cli.cluster

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getClusterContainers
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable


class CommandListClusterContainers : PmcCommand(
        name = "containers",
        help = "List all existing cluster containers",
        printHelpOnEmptyArgs = false
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val clusterName by nameOption("Cluster name").required()

    override fun run() {
        val containers = client.getClusterContainers(clusterName)
        echo(pmcTable(
                "containers",
                listOf("Container name", "deployer"),
                containers.map { listOf(it.name, it.deployer) }
        ))
    }
}