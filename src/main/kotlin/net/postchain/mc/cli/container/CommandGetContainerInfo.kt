package net.postchain.mc.cli.container

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.anchoring.anchoring_chain_cluster.ContainerResourceUsageStatistics
import net.postchain.anchoring.anchoring_chain_cluster.getLatestResourceUsageStatistics
import net.postchain.chain0.common.queries.getContainerBlockchain
import net.postchain.chain0.common.queries.getContainerData
import net.postchain.chain0.nm_api.nmGetContainerLimits
import net.postchain.chain0.version.apiVersion
import net.postchain.economy.economy_chain.LeaseData
import net.postchain.economy.economy_chain.getLeaseByContainerName
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.base.CLUSTER_ANCHORING_CHAIN_RESOURCE_USAGE_VERSION
import net.postchain.mc.cli.economy.getEconomyChainClientOrNull
import net.postchain.mc.cli.util.PmcClientConfigOption
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date

class CommandGetContainerInfo : PmcCommand(
        name = "info",
        help = "Get information about a container"
) {

    private val config by pmcConfigOption()

    private val name by nameOption("Container Name").required()
    private val resourceUsage by option("-ru", "--resource-usage", help = "Display container resource usage per node")
            .flag()

    override fun run() {
        showContainerInfo(config, name, resourceUsage)
    }
}

fun CliktCommand.showContainerInfo(
        config: PmcClientConfigOption,
        name: String,
        resourceUsage: Boolean = false
) {
    val client = config.chromiaClient.getDirectoryChainClient()
    val info = client.getContainerData(name)
    val lease = getEconomyChainClientOrNull(config)?.getLeaseByContainerName(name)

    if (!terminal.terminalInfo.outputInteractive) {
        echo("{")
        echo(""""container_info": """, trailingNewline = false)
    }

    echo(pmcTable {
        body {
            row("Name:", info.name)
            row("Cluster:", info.cluster)
            row("Deployer:", info.deployer)
            row(
                    "Proposed by:",
                    listOf(info.proposedByPubkey.toHex(), info.proposedByName)
                            .filter { it.isNotEmpty() }
                            .joinToString(" / ")
            )
            row("System:", info.system.toString())
            info.subnodeImage?.let { row("Subnode image:", it) }
            if (info.jarExtensions?.isNotEmpty() == true) {
                row("Subnode JAR extensions:", info.jarExtensions.joinToString(", "))
            }
            info.state?.let { row("State:", it.toString()) }
            lease?.let { row("Lease expires:", formatLeaseExpiration(it)) }
        }
    })

    val limits = client.nmGetContainerLimits(name)
    if (!terminal.terminalInfo.outputInteractive) {
        echo(""","resource_limits": """, trailingNewline = false)
    }
    echo(pmcTable(
            "resources limits",
            listOf("Resource type", "Value"),
            limits.map {
                listOf(it.key, when (it.key) {
                    ContainerResourceLimitType.cpu.name -> "${it.value} %"
                    ContainerResourceLimitType.ram.name -> "${it.value} MiB"
                    ContainerResourceLimitType.storage.name -> "${it.value} MiB"
                    ContainerResourceLimitType.io_read.name -> "${it.value} MiB/s"
                    ContainerResourceLimitType.io_write.name -> "${it.value} MiB/s"
                    else -> it.value.toString()
                })
            }
    ))

    if (resourceUsage) {
        val clusterAnchoringClient = config.chromiaClient.getClusterAnchoringClient(info.cluster)
        if (clusterAnchoringClient.apiVersion() < CLUSTER_ANCHORING_CHAIN_RESOURCE_USAGE_VERSION) {
            throw CliktError("The cluster is running an older version of the cluster anchoring chain, which does not support resource usage statistics")
        } else {
            val resourceUsageStats = clusterAnchoringClient.getLatestResourceUsageStatistics(name, null, null)
            if (!terminal.terminalInfo.outputInteractive) {
                echo(""","resource_usage": """, trailingNewline = false)
            }
            echo(pmcTable(
                    "resource usage",
                    listOf("Node", "Resource type", "Value", "Timestamp"),
                    resourceUsageStats
                            .sortedWith(compareBy<ContainerResourceUsageStatistics> { it.resourceType.name }
                                    .thenBy { it.nodePubkey.toHex() })
                            .map {
                        listOf(
                                it.nodePubkey.toHex(),
                                it.resourceType.name,
                                it.metricValue.toString(),
                                Date.from(Instant.ofEpochMilli(it.measurementTime)).toString()
                        )
                    }
            ))
        }
    }

    val blockchains = client.getContainerBlockchain(name)
    if (!terminal.terminalInfo.outputInteractive) {
        echo(""","blockchains": """, trailingNewline = false)
    }
    echo(pmcTable(
            "blockchains",
            listOf("Name", "Rid", "System", "State"),
            blockchains.map {
                listOf(it.name, it.rid.toHex(), it.system.toString(), it.state.toString())
            }
    ))

    if (!terminal.terminalInfo.outputInteractive) {
        echo("}")
    }
}

private val expirationFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC)

fun formatLeaseExpiration(lease: LeaseData): String =
        expirationFormatter.format(Instant.ofEpochMilli(lease.expireTimeMillis)) + if (lease.expired) " (expired)" else ""

enum class ContainerResourceLimitType {
    max_blockchains,
    cpu,
    ram,
    storage,
    io_read,
    io_write
}
