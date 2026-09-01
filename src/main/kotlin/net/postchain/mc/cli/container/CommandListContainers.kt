package net.postchain.mc.cli.container

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.input.interactiveSelectList
import net.postchain.chain0.common.queries.getBlockchainInfoList
import net.postchain.chain0.common.queries.getContainers
import net.postchain.economy.economy_chain.LeaseData
import net.postchain.economy.economy_chain.getLeaseByContainerName
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.base.NAME_LENGTH_MAX
import net.postchain.mc.cli.economy.getEconomyChainClientOrNull
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

    override fun run() {
        val containers = client.getContainers()
        val bcs = client.getBlockchainInfoList(false).groupBy { it.container }
        if (interactive && containers.isNotEmpty() && containers.size < terminal.size.height) {
            terminal.interactiveSelectList(containers.map {
                "${it.name} - ${bcs[it.name]?.joinToString(", ") { bc -> bc.name } ?: ""}"
            }, "Select container")?.let {
                showContainerInfo(config, it.split(' ').first())
            }
        } else {
            val leases = leasesByContainer(containers.map { it.name })
            echo(pmcTable(
                    "containers",
                    headers(leases != null),
                    containers.map {
                        listOf(it.name, it.cluster, it.deployer) +
                                expirationColumn(leases, it.name) +
                                listOf(bcs[it.name]?.joinToString(", ") { bc -> bc.name } ?: "")
                    },
                    0 to NAME_LENGTH_MAX,
                    interactive
            ))
            if (interactive && containers.isNotEmpty()) {
                promptForIndex(containers)?.let {
                    showContainerInfo(config, containers[it].name)
                }
            }
        }
    }

    /**
     * Lease expiration is kept on economy chain, which is not present on all networks.
     */
    private fun leasesByContainer(containerNames: List<String>): Map<String, LeaseData?>? =
            getEconomyChainClientOrNull(config)?.let { economyChainClient ->
                containerNames.parallelStream()
                        .map { it to economyChainClient.getLeaseByContainerName(it) }
                        .toList()
                        .toMap()
            }

    private fun headers(withExpiration: Boolean) =
            listOf("Name", "Cluster", "Deployer voter set") +
                    (if (withExpiration) listOf(LEASE_EXPIRES_HEADER) else listOf()) +
                    listOf("Blockchains")

    private fun expirationColumn(leases: Map<String, LeaseData?>?, containerName: String) =
            if (leases == null) listOf() else listOf(leases[containerName]?.let { formatLeaseExpiration(it) } ?: "")
}
