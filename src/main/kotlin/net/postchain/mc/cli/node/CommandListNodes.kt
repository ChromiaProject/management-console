package net.postchain.mc.cli.node

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.input.interactiveSelectList
import net.postchain.chain0.common.queries.GetAllNodesResult
import net.postchain.chain0.common.queries.getAllNodes
import net.postchain.chain0.common.queries.getClusterNodes
import net.postchain.common.hexStringToByteArray
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.base.PUBKEY_LENGTH
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandListNodes : PmcCommand(
        name = "list",
        help = "List all nodes, optionally filtered by cluster",
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()
    private val cluster by option("--cluster", help = "Only list nodes belonging to the given cluster", metavar = "CLUSTER_NAME")

    private val headers = listOf("Pubkey", "Host", "Port", "REST API", "Territory", "Active", "Provided by")

    override fun run() {
        val nodes = client.getAllNodes(includeInactive = true)
                .filter(clusterFilter())
        if (interactive && nodes.isNotEmpty() && nodes.size < terminal.size.height) {
            terminal.interactiveSelectList(nodes.map {
                "${it.info.pubkey.toHex()} - ${it.info.host}"
            }, "Select node")?.let {
                showNodeInfo(client, PubKey(it.split(' ').first().hexStringToByteArray()))
            }
        } else {
            echo(pmcTable(
                    "nodes",
                    headers,
                    nodes.map {
                        listOf(
                                it.info.pubkey.toHex(),
                                it.info.host,
                                it.info.port.toString(),
                                it.info.apiUrl,
                                it.info.territory ?: "",
                                it.active.toString(),
                                it.provider.pubkey.toHex())
                    },
                    0 to PUBKEY_LENGTH,
                    interactive
            ))
            if (interactive && nodes.isNotEmpty()) {
                promptForIndex(nodes)?.let {
                    showNodeInfo(client, PubKey(nodes[it].info.pubkey))
                }
            }
        }
    }

    private fun clusterFilter(): (GetAllNodesResult) -> Boolean {
        val clusterNodePubkeys = cluster?.let { name -> client.getClusterNodes(name).map { it.pubkey }.toSet() }
        return { clusterNodePubkeys == null || it.info.pubkey in clusterNodePubkeys }
    }
}
