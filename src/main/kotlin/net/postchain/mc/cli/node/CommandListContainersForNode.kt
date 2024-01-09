package net.postchain.mc.cli.node

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getNodeContainers
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.cli.util.pubkeyOption

class CommandListContainersForNode : CliktCommand(
        name = "containers",
        help = "List containers for node"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val key by pubkeyOption()

    override fun run() {
        val containers = client.getNodeContainers(key)
        echo(pmcTable(
                "containers",
                listOf("Name", "Cluster", "Deployer"),
                containers.map { listOf(it.name, it.cluster, it.deployer) }
        ))
    }
}
