package net.postchain.mc.cli.container

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.input.interactiveSelectList
import net.postchain.chain0.common.queries.getBlockchainInfoList
import net.postchain.chain0.common.queries.getContainers
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.base.NAME_LENGTH_MAX
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandListContainers : PmcCommand(
        name = "list",
        help = "List all existing containers",
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()

    private val headers = listOf("Name", "Cluster", "Deployer voter set", "Blockchains")

    override fun run() {
        val containers = client.getContainers()
        val bcs = client.getBlockchainInfoList(false).groupBy { it.container }
        if (interactive && containers.isNotEmpty() && containers.size < terminal.size.height) {
            terminal.interactiveSelectList(containers.map {
                "${it.name} - ${bcs[it.name]?.joinToString(", ") { bc -> bc.name } ?: ""}"
            }, "Select container")?.let {
                showContainerInfo(config.chromiaClient, it.split(' ').first())
            }
        } else {
            echo(pmcTable(
                    "containers",
                    headers,
                    containers.map {
                        listOf(it.name, it.cluster, it.deployer, bcs[it.name]?.joinToString(", ") { bc -> bc.name }
                                ?: "")
                    },
                    0 to NAME_LENGTH_MAX,
                    interactive
            ))
            if (interactive && containers.isNotEmpty()) {
                promptForIndex(containers)?.let {
                    showContainerInfo(config.chromiaClient, containers[it].name)
                }
            }
        }
    }
}
