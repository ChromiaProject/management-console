package net.postchain.mc.cli.container

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.table.ColumnWidth
import net.postchain.chain0.common.queries.BlockchainInfo
import net.postchain.chain0.common.queries.getBlockchainInfoList
import net.postchain.chain0.common.queries.getContainers
import net.postchain.chain0.model.BlockchainState
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.base.NAME_LENGTH_MAX
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.compatibility.ApiCompatV3.getBlockchainInfoListV3

class CommandListContainers : CliktCommand(
        name = "list",
        help = "List all existing containers"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()

    private val headers = listOf("Name", "Cluster", "Deployer voter set", "Blockchains")

    override fun run() {
        val containers = client.getContainers()
        if (containers.isEmpty()) {
            echo("No containers")
        } else {
            val bcs = when {
                client.apiVersion() <= 3 -> {
                    client.getBlockchainInfoListV3(false).map {
                        BlockchainInfo(
                                it.rid, it.name, BlockchainState.RUNNING, it.container, it.cluster, null
                        )
                    }.groupBy { it.container }
                }

                else -> client.getBlockchainInfoList(false).groupBy { it.container }
            }
            echo(defaultTable {
                column(0) {
                    width = ColumnWidth.Fixed(if (interactive) 3 else NAME_LENGTH_MAX + 1)
                }
                header { rowFrom(if (interactive) listOf("#") + headers else headers) }
                body {
                    containers.forEachIndexed { index, it ->
                        val columns = listOf(it.name, it.cluster, it.deployer, bcs[it.name]?.joinToString(", ") { bc -> bc.name } ?: "")
                        rowFrom(if (interactive) listOf(index.toString()) + columns else columns)
                    }
                }
            })
            if (interactive) {
                promptForIndex(containers)?.let {
                    showContainerInfo(client, containers[it].name)
                }
            }
        }
    }
}