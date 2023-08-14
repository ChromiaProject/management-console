package net.postchain.mc.cli.node

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getNodeData
import net.postchain.chain0.common.queries.listClustersOfNode
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.requiredPubkeyOption
import net.postchain.mc.cli.util.pmcConfigOption

class CommandGetNodeInfo : CliktCommand(
        name = "info",
        help = "Get node info for given node pubkey"
) {
    private val config by pmcConfigOption()

    private val key by requiredPubkeyOption()

    override fun run() {
        val client = config.client
        val node = client.getNodeData(key)
        echo(defaultTable {
            body {
                row("Active:", "${node.active}")
                row("Host:", node.host)
                row("Port:", "${node.port}")
                row("REST API:", node.apiUrl)
                row("Provided by:", node.provider.toHex())
                node.clusterUnits?.let { row("Cluster Units:", it.toString()) }
                val clusters = client.listClustersOfNode(PubKey(node.pubkey))
                row("Used by clusters:", "$clusters")
            }
        })
    }
}
