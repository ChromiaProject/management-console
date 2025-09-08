package net.postchain.mc.cli.cluster

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getClusterContainerUnitLimits
import net.postchain.chain0.common.queries.getClusterContainers
import net.postchain.chain0.common.queries.getClusterData
import net.postchain.chain0.common.queries.getClusterNodes
import net.postchain.chain0.common.queries.getClusterProviders
import net.postchain.chain0.common.queries.getClusterReplicaNodes
import net.postchain.chain0.common.queries.getClusterSubnodeImages
import net.postchain.chain0.version.apiVersion
import net.postchain.client.core.PostchainReadClient
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV28.getClusterDataV28
import net.postchain.mc.compatibility.ApiCompatV33.getClusterDataV33
import net.postchain.mc.compatibility.ApiCompatV56.getClusterDataV56

class CommandGetClusterInfo : PmcCommand(
        name = "info",
        help = "Get information about a cluster"
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOption("Cluster Name").required()

    override fun run() {
        val apiVersion = client.apiVersion()
        showClusterInfo(apiVersion, client, name)
    }
}

fun CliktCommand.showClusterInfo(apiVersion: Long, client: PostchainReadClient, name: String) {
    if (!terminal.terminalInfo.outputInteractive) {
        echo("{")
        echo(""""basic": """, trailingNewline = false)
    }
    when {
        apiVersion >= 57 -> {
            val info = client.getClusterData(name)
            echo(pmcTable {
                body {
                    row("Name:", info.name)
                    row("Governor:", info.governor)
                    row("Is Operational:", info.isOperational.toString())
                    info.numberOfNodes?.let { row("Number of nodes:", it.toString()) }
                    info.clusterUnits?.let { row("Cluster Units:", it.toString()) }
                    info.extraStorage?.let { row("Extra Storage:", it.toString()) }
                    info.containerUnitsAvailable?.let { row("Container Units available:", it.toString()) }
                    info.extraStorageAvailable?.let { row("Extra Storage available:", it.toString()) }
                }
            })
        }

        apiVersion >= 34 -> {
            val info = client.getClusterDataV56(name)
            echo(pmcTable {
                body {
                    row("Name:", info.name)
                    row("Governor:", info.governor)
                    row("Is Operational:", info.isOperational.toString())
                    info.clusterUnits?.let { row("Cluster Units:", it.toString()) }
                    info.extraStorage?.let { row("Extra Storage:", it.toString()) }
                    info.containerUnitsAvailable?.let { row("Container Units available:", it.toString()) }
                    info.extraStorageAvailable?.let { row("Extra Storage available:", it.toString()) }
                }
            })
        }

        apiVersion >= 29 -> {
            val info = client.getClusterDataV33(name)
            echo(pmcTable {
                body {
                    row("Name:", info.name)
                    row("Governor:", info.governor)
                    row("Is Operational:", info.isOperational.toString())
                    info.clusterUnits?.let { row("Cluster Units:", it.toString()) }
                    info.extraStorage?.let { row("Extra Storage:", it.toString()) }
                    info.containerUnitsAvailable?.let { row("Container Units available:", it.toString()) }
                    info.extraStorageAvailable?.let { row("Extra Storage available:", it.toString()) }
                    info.clusterClass?.let { row("Cluster class:", it) }
                }
            })
        }

        else -> {
            val info = client.getClusterDataV28(name)
            echo(pmcTable {
                body {
                    row("Name:", info.name)
                    row("Governor:", info.governor)
                    row("Is Operational:", info.isOperational.toString())
                    info.clusterUnits?.let { row("Cluster Units:", it.toString()) }
                    info.extraStorage?.let { row("Extra Storage:", it.toString()) }
                }
            })
        }
    }

    if (apiVersion >= 88) {
        val clusterUnit = client.getClusterContainerUnitLimits(name)
        if (!terminal.terminalInfo.outputInteractive) {
            echo(""","container_unit_limits": """, trailingNewline = false)
        }
        echo(pmcTable(
                "container unit limits",
                listOf("Resource", "Limit"),
                listOf(
                        listOf("CPU", clusterUnit.cpu.toString()),
                        listOf("RAM", clusterUnit.ram.toString()),
                        listOf("I/O read", clusterUnit.ioRead.toString()),
                        listOf("I/O write", clusterUnit.ioWrite.toString()),
                        listOf("Storage", clusterUnit.storage.toString())
                )))
    }

    if (apiVersion >= 56) {
        val images = client.getClusterSubnodeImages(name)
        if (!terminal.terminalInfo.outputInteractive) {
            echo(""","subnode_images": """, trailingNewline = false)
        }
        echo(pmcTable(
                "subnode images",
                listOf("Name", "URL", "digest"),
                images.map { image ->
                    listOf(image.name, image.url, image.digest)
                }
        ))
    }

    val clusterProviders = client.getClusterProviders(name)
    if (!terminal.terminalInfo.outputInteractive) {
        echo(""","providers": """, trailingNewline = false)
    }
    echo(pmcTable(
            "providers",
            listOf("Provider", "Alias"),
            clusterProviders.map { provider ->
                listOf(provider.pubkey.toString(), provider.name)
            }
    ))

    val clusterNodes = client.getClusterNodes(name)
    if (!terminal.terminalInfo.outputInteractive) {
        echo(""","nodes": """, trailingNewline = false)
    }
    echo(pmcTable(
            "nodes",
            listOf("Node", "Host", "API url"),
            clusterNodes.map { node ->
                listOf(node.pubkey.toString(), "${node.host}:${node.port}", node.apiUrl)
            }
    ))

    val clusterReplicas = client.getClusterReplicaNodes(name)
    if (!terminal.terminalInfo.outputInteractive) {
        echo(""","replica_nodes": """, trailingNewline = false)
    }
    echo(pmcTable(
            "replica nodes",
            listOf("Replica node", "Address"),
            clusterReplicas.map { node ->
                listOf(node.pubkey.toString(), "${node.host}:${node.port} / ${node.apiUrl}")
            }
    ))

    val containers = client.getClusterContainers(name)
    if (!terminal.terminalInfo.outputInteractive) {
        echo(""","containers": """, trailingNewline = false)
    }
    echo(pmcTable(
            "containers",
            listOf("Container", "Deployer"),
            containers.map {
                listOf(it.name, it.deployer)
            }
    ))
    
    if (!terminal.terminalInfo.outputInteractive) {
        echo("}")
    }
}
