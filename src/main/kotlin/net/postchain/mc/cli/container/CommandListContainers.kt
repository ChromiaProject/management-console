package net.postchain.mc.cli.container

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.BlockchainInfo
import net.postchain.chain0.common.queries.getBlockchainInfoList
import net.postchain.chain0.common.queries.getContainers
import net.postchain.chain0.model.BlockchainState
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.base.NAME_LENGTH_MAX
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV3.getBlockchainInfoListV3

class CommandListContainers : PmcCommand(
        name = "list",
        help = "List all existing containers"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()

    private val headers = listOf("Name", "Cluster", "Deployer voter set", "Blockchains")

    override fun run() {
        val containers = client.getContainers()
        val bcs = when {
            client.apiVersion() <= 3 -> {
                client.getBlockchainInfoListV3(false).map {
                    BlockchainInfo(
                            it.rid, it.name, BlockchainState.RUNNING, it.container, it.cluster, null,
                            null, null, null, null
                    )
                }.groupBy { it.container }
            }

            else -> client.getBlockchainInfoList(false).groupBy { it.container }
        }
        echo(pmcTable(
                "containers",
                headers,
                containers.map {
                    listOf(it.name, it.cluster, it.deployer, bcs[it.name]?.joinToString(", ") { bc -> bc.name } ?: "")
                },
                0 to NAME_LENGTH_MAX,
                interactive
        ))
        if (interactive && containers.isNotEmpty()) {
            promptForIndex(containers)?.let {
                showContainerInfo(client, containers[it].name)
            }
        }
    }
}
