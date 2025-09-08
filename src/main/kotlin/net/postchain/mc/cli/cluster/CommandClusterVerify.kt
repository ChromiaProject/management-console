package net.postchain.mc.cli.cluster

import com.chromia.cli.base.formatter.jsonTable
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.cm_api.cmGetClusterBlockchains
import net.postchain.chain0.cm_api.cmGetClusterInfo
import net.postchain.chain0.cm_api.cmGetSystemAnchoringChain
import net.postchain.client.request.Endpoint
import net.postchain.client.request.EndpointPool
import net.postchain.common.BlockchainRid
import net.postchain.common.wrap
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.util.BlockHeightClient
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.cli.util.prettyTable

class CommandClusterVerify : PmcCommand(
        name = "verify",
        help = "Verify cluster status"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val cluster by nameOption("name of cluster").required()

    override fun run() {
        val clusterInfo = client.cmGetClusterInfo(cluster)
        val clusterEndpoints = clusterInfo.peers.map { Endpoint.sanitizeUrl(it.apiUrl) }.let { EndpointPool.default(it) }

        if (terminal.terminalInfo.outputInteractive) {
            echo("Verifying cluster $cluster")
            echo(pmcTable(
                    "nodes",
                    listOf("#", "Pubkey", "Url"),
                    clusterInfo.peers.mapIndexed { index, peer -> listOf((index + 1).toString(), PubKey(peer.pubkey).hex(), peer.apiUrl) }
            ))
        }

        val blockHeightClient = BlockHeightClient(config.chromiaClient)
        val chains = client.cmGetClusterBlockchains(cluster).parallelStream().map { bc ->
            val blockchainRid = BlockchainRid(bc)
            val anchoringChain = when (bc.wrap()) {
                client.cmGetSystemAnchoringChain()?.wrap() -> null
                clusterInfo.anchoringChain -> client.cmGetSystemAnchoringChain()?.wrap()
                else -> clusterInfo.anchoringChain
            }
            Triple(
                    blockchainRid,
                    blockHeightClient.getLastAnchoredBlockHeight(anchoringChain, clusterEndpoints, blockchainRid),
                    clusterInfo.peers.map { it to blockHeightClient.getCurrentBlockHeightOnPeer(it, blockchainRid) }
            )
        }.toList()

        echo(if (terminal.terminalInfo.outputInteractive) {
            prettyTable(
                    "heights on cluster chains",
                    listOf("Blockchain RID", "Anchored", *clusterInfo.peers.mapIndexed { index, _ -> (index + 1).toString() }.toTypedArray()),
                    chains.map { (blockchainRid, anchoredHeight, nodeHeights) ->
                        listOf(blockchainRid.toHex(), anchoredHeight.toString(), *nodeHeights.map { it.second.toString() }.toTypedArray())
                    }
            )
        } else jsonTable {
            header {
                row("Blockchain RID", "Anchored", *clusterInfo.peers.map { it.pubkey.toHex() }.toTypedArray())
            }
            body {
                chains.forEach { (blockchainRid, anchoredHeight, nodeHeights) ->
                    row(blockchainRid.toHex(), anchoredHeight, *nodeHeights.map { it.second }.toTypedArray())
                }
            }
        })
    }
}
