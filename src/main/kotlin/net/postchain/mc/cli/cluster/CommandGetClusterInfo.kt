package net.postchain.mc.cli.cluster

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
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV28.getClusterDataV28
import net.postchain.mc.compatibility.ApiCompatV33.getClusterDataV33

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
        apiVersion >= 34 -> {
            val info = client.getClusterData(name)
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

    val clusterProviders = client.getClusterProviders(name)
    echo(pmcTable(
            "providers",
            listOf("Provider", "Alias"),
            clusterProviders.map { provider ->
                listOf(provider.pubkey.toString(), provider.name)
            }
    ))

    val clusterNodes = client.getClusterNodes(name)
    echo(pmcTable(
            "nodes",
            listOf("Node", "Host", "API url"),
            clusterNodes.map { node ->
                listOf(node.pubkey.toString(), "${node.host}:${node.port}", node.apiUrl)
            }
    ))

    val clusterReplicas = client.getClusterReplicaNodes(name)
    echo(pmcTable(
            "replica nodes",
            listOf("Replica node", "Address"),
            clusterReplicas.map { node ->
                listOf(node.pubkey.toString(), "${node.host}:${node.port} / ${node.apiUrl}")
            }
    ))

    val containers = client.getClusterContainers(name)
    echo(pmcTable(
            "containers",
            listOf("Container", "Deployer"),
            containers.map {
                listOf(it.name, it.deployer)
            }
    ))
}
