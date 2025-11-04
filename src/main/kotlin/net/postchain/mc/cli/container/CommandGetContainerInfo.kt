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
import net.postchain.d1.client.StandardChromiaClient
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.base.CLUSTER_ANCHORING_CHAIN_RESOURCE_USAGE_VERSION
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV2
import net.postchain.mc.compatibility.ApiCompatV3.getContainerBlockchainV3
import java.time.Instant
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
        showContainerInfo(config.chromiaClient, name, resourceUsage)
    }
}

fun CliktCommand.showContainerInfo(
        chromiaClient: StandardChromiaClient,
        name: String,
        resourceUsage: Boolean = false
) {
    val client = chromiaClient.getDirectoryChainClient()
    val info = client.getContainerData(name)

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
                    ApiCompatV2.ContainerResourceLimitType.cpu.name -> "${it.value} %"
                    ApiCompatV2.ContainerResourceLimitType.ram.name -> "${it.value} MiB"
                    ApiCompatV2.ContainerResourceLimitType.storage.name -> "${it.value} MiB"
                    ApiCompatV2.ContainerResourceLimitType.io_read.name -> "${it.value} MiB/s"
                    ApiCompatV2.ContainerResourceLimitType.io_write.name -> "${it.value} MiB/s"
                    else -> it.value.toString()
                })
            }
    ))

    if (resourceUsage) {
        val clusterAnchoringClient = chromiaClient.getClusterAnchoringClient(info.cluster)
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

    val apiVersion = client.apiVersion()
    when {
        apiVersion >= 4 -> {
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
        }

        else -> {
            val blockchains = client.getContainerBlockchainV3(name)
            if (!terminal.terminalInfo.outputInteractive) {
                echo(""","blockchains": """, trailingNewline = false)
            }
            echo(pmcTable(
                    "blockchains",
                    listOf("Name", "Rid", "System", "Active"),
                    blockchains.map {
                        listOf(it.name, it.rid.toHex(), it.system.toString(), it.active.toString())
                    }
            ))
        }
    }

    if (!terminal.terminalInfo.outputInteractive) {
        echo("}")
    }
}
