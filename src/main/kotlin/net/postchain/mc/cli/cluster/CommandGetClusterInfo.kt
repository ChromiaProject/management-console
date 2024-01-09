package net.postchain.mc.cli.cluster

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getClusterContainers
import net.postchain.chain0.common.queries.getClusterData
import net.postchain.chain0.common.queries.getClusterNodes
import net.postchain.chain0.common.queries.getClusterProviders
import net.postchain.chain0.common.queries.getClusterReplicaNodes
import net.postchain.chain0.version.apiVersion
import net.postchain.client.core.PostchainClient
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.compatibility.ApiCompatV28.getClusterDataV28

class CommandGetClusterInfo : CliktCommand(
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

fun CliktCommand.showClusterInfo(apiVersion: Long, client: PostchainClient, name: String) {

    when {
        apiVersion >= 29 -> {
            val info = client.getClusterData(name)
            echo(defaultTable {
                body {
                    row("Name:", info.name)
                    row("Governor:", info.governor)
                    row("Is Operational:", info.isOperational.toString())
                    info.clusterUnits?.let { row("Cluster Units:", it.toString()) }
                    info.extraStorage?.let { row("Extra Storage:", it.toString()) }
                    info.containerUnitsAvailable?.let { row("Container Units available:", it.toString()) }
                    info.extraStorageAvailable?.let { row("Extra Storage available:", it.toString()) }
                    info.clusterClass?.let { row("Cluster class:", it) }
                    row()
                }
            })
        }

        else -> {
            val info = client.getClusterDataV28(name)
            echo(defaultTable {
                body {
                    row("Name:", info.name)
                    row("Governor:", info.governor)
                    row("Is Operational:", info.isOperational.toString())
                    info.clusterUnits?.let { row("Cluster Units:", it.toString()) }
                    info.extraStorage?.let { row("Extra Storage:", it.toString()) }
                    row()
                }
            })
        }
    }

    val clusterProviders = client.getClusterProviders(name)
    if (clusterProviders.isNotEmpty()) {
        echo(defaultTable {
            header { row("Provider", "Alias") }
            body {
                clusterProviders.forEach { provider ->
                    row(provider.pubkey.toString(), provider.name)
                }
            }
        })
    } else {
        echo("No providers")
    }

    val clusterNodes = client.getClusterNodes(name)
    if (clusterNodes.isNotEmpty()) {
        echo(defaultTable {
            header { row("Node", "Host", "API url") }
            body {
                clusterNodes.forEach { node ->
                    row(node.pubkey.toString(), "${node.host}:${node.port}", node.apiUrl)
                }
            }
        })
    } else {
        echo("No nodes")
    }

    val clusterReplicas = client.getClusterReplicaNodes(name)
    if (clusterReplicas.isNotEmpty()) {
        echo(defaultTable {
            header { row("Replica node", "Address") }
            body {
                clusterReplicas.forEach { node ->
                    row(node.pubkey.toString(), "${node.host}:${node.port} / ${node.apiUrl}")
                }

            }
        })
    } else {
        echo("No replica nodes")
    }

    val containers = client.getClusterContainers(name)
    if (containers.isNotEmpty()) {
        echo(defaultTable {
            header { row("Container", "Deployer") }
            body {
                containers.forEach {
                    row(it.name, it.deployer)
                }
            }
        })
    } else {
        echo("No containers")
    }
}
