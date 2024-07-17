package net.postchain.mc.cli.cluster

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.cm_api.CmClusterInfo
import net.postchain.chain0.cm_api.cmGetClusterBlockchains
import net.postchain.chain0.cm_api.cmGetClusterInfo
import net.postchain.chain0.cm_api.cmGetSystemAnchoringChain
import net.postchain.client.request.Endpoint
import net.postchain.client.request.EndpointPool
import net.postchain.client.request.RandomizedEndpointPool
import net.postchain.common.BlockchainRid
import net.postchain.common.wrap
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.util.BlockHeightClient
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandClusterVerify : CliktCommand(
        name = "verify",
        help = "Verify cluster status"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val cluster by nameOption("name of cluster").required()

    override fun run() {
        val clusterInfo = client.cmGetClusterInfo(cluster)
        val clusterEndpoints = clusterInfo.peers.map { Endpoint.sanitizeUrl(it.apiUrl) }.let { EndpointPool.default(it) }

        if (terminal.info.outputInteractive) echo("Verifying cluster $cluster")

        echo(pmcTable(
                "nodes",
                listOf("Pubkey", "Url"),
                clusterInfo.peers.map { listOf(PubKey(it.pubkey).toShortHex(), it.apiUrl) }
        ))

        analyzeBlockchains(client.cmGetClusterBlockchains(cluster), clusterInfo, clusterEndpoints)
    }

    private fun analyzeBlockchains(chainsToAnalyze: Collection<ByteArray>, clusterInfo: CmClusterInfo, clusterEndpoints: RandomizedEndpointPool) {
        val blockHeightClient = BlockHeightClient(client)
        echo(pmcTable(
                "cluster chains",
                listOf("Blockchain RID", "Anchored height", *clusterInfo.peers.map { PubKey(it.pubkey).toShortHex() }.toTypedArray()),
                chainsToAnalyze.map { BlockchainRid(it) }.map { bc ->
                    val anchoringChain = when (bc.wData) {
                        client.cmGetSystemAnchoringChain()?.wrap() -> null
                        clusterInfo.anchoringChain -> client.cmGetSystemAnchoringChain()?.wrap()
                        else -> clusterInfo.anchoringChain
                    }
                    listOf(
                            bc.toHex(),
                            blockHeightClient.getLastAnchoredBlockHeight(anchoringChain, clusterEndpoints, bc).toString(),
                            *clusterInfo.peers.map { blockHeightClient.getCurrentBlockHeightOnPeer(it, bc).toString() }.toTypedArray()
                    )
                }
        ))
    }
}
