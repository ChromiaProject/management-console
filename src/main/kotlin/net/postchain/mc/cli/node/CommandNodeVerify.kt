package net.postchain.mc.cli.node

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.anchoring.anchoring_chain_common.getLastAnchoredBlock
import net.postchain.chain0.cm_api.cmGetClusterInfo
import net.postchain.chain0.cm_api.cmGetSystemAnchoringChain
import net.postchain.chain0.common.queries.getBlockchainCluster
import net.postchain.chain0.common.queries.getNodeData
import net.postchain.chain0.common.queries.listClustersOfNode
import net.postchain.chain0.nm_api.nmComputeBlockchainInfoList
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.client.request.SingleEndpointPool
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.cli.util.pubkeyOption
import net.postchain.mc.network.NodeVerifier

class CommandNodeVerify : CliktCommand(
        name = "verify",
        help = "Verify node status"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val key by pubkeyOption()

    private fun Boolean?.isOk(): String = this?.let { if (this) "OK" else "Bad" } ?: "Bad"
    override fun run() {
        val node = client.getNodeData(key)
        val nodeVerifier = NodeVerifier(client.config, client.cmGetSystemAnchoringChain()?.let { BlockchainRid(it) })
        val clusters = client.listClustersOfNode(key)
        val clusterAnchorChains = clusters.map { client.cmGetClusterInfo(it) }.associate { it.name to BlockchainRid(it.anchoringChain) }
        val clusterAnchorChainsHeights = clusterAnchorChains.map { it.key to nodeVerifier.verifyBlockchain(it.value, node.apiUrl).second }
        val nodeStatus = nodeVerifier.verifyApi(node.apiUrl)

        echo(pmcTable {
            body {
                row("Node", "${node.pubkey}")
                row("Url", node.apiUrl)
                row("System chains", nodeStatus.responds.isOk())
                row("Management chain", "${nodeStatus.height}")
                row("System anchoring chain", "${nodeStatus.systemAnchorHeight}")
                row("Cluster anchor chains", if (clusterAnchorChains.isEmpty()) "no clusters node is running in" else clusterAnchorChainsHeights.joinToString(", "))
            }
        })

        val blockchains = client.nmComputeBlockchainInfoList(key.data).filter { !it.system }.map { BlockchainRid(it.rid) }

        val bcStatuses = blockchains.associateWith { blockchainRid ->
            val bcHeight = nodeVerifier.verifyBlockchain(blockchainRid, node.apiUrl)
            val anchoredHeight = client.getBlockchainCluster(blockchainRid).let { bcCluster ->
                clusterAnchorChains[bcCluster]
            }?.let { anchoringBrid ->
                PostchainClientImpl(client.config.copy(
                        blockchainRid = anchoringBrid,
                        endpointPool = SingleEndpointPool(node.apiUrl)
                )).getLastAnchoredBlock(blockchainRid)?.blockHeight
            }

            Triple(anchoredHeight, bcHeight.first, bcHeight.second)
        }

        echo(pmcTable(
                "blockchains",
                listOf("Blockchain", "Responds", "Height", "Anchored Height", "Synchronized"),
                bcStatuses.map { (brid, status) ->
                    listOf(
                            brid.toHex(),
                            "${status.second}",
                            "${status.third}",
                            "${status.first}",
                            status.third?.let { if (it < (status.first ?: 0)) "NO" else "Yes" } ?: "NO"
                    )
                }
        ))
    }
}
