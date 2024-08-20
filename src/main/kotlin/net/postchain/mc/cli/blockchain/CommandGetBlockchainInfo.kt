package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.mordant.rendering.TextAlign
import net.postchain.chain0.cm_api.CmPeerInfo
import net.postchain.chain0.cm_api.cmGetClusterInfo
import net.postchain.chain0.cm_api.cmGetSystemAnchoringChain
import net.postchain.chain0.common.queries.getBlockchainInfo
import net.postchain.chain0.common.queries.getBlockchainReplicas
import net.postchain.chain0.common.queries.getContainerData
import net.postchain.chain0.common.queries.getImportingForeignBlockchainInfo
import net.postchain.chain0.common.queries.getMovingBlockchainInfo
import net.postchain.chain0.common.queries.getNodeData
import net.postchain.chain0.common.queries.getUnarchivingBlockchainInfo
import net.postchain.chain0.version.apiVersion
import net.postchain.client.core.PostchainClient
import net.postchain.client.request.Endpoint
import net.postchain.client.request.EndpointPool
import net.postchain.common.BlockchainRid
import net.postchain.common.wrap
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.BlockHeightClient
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV33.getImportingForeignBlockchainInfoV33

class CommandGetBlockchainInfo : CliktCommand(
        name = "info",
        help = "Get blockchain info"
) {
    private val config by pmcConfigOption()

    private val blockchainRID by blockchainRidOption().required()

    override fun run() {
        val client = config.client
        val apiVersion = client.apiVersion()
        if (apiVersion >= 17) {
            showBlockchainInfo(client, apiVersion, blockchainRID)
        } else {
            throw CliktError("blockchain info requires directory chain version 17, found version $apiVersion")
        }
    }
}

internal fun CliktCommand.showBlockchainInfo(client: PostchainClient, apiVersion: Long, blockchainRid: BlockchainRid) {
    val blockchainInfo = client.getBlockchainInfo(blockchainRid.data)
            ?: throw CliktError("Blockchain with rid $blockchainRid not found")

    // Basic info
    echo(pmcTable {
        captionTop("Basic info:", TextAlign.LEFT)
        body {
            row("Name", blockchainInfo.name)
            row("RID", blockchainInfo.rid)
            row("State", blockchainInfo.state)
            row("Container", blockchainInfo.container)
            row("Cluster", blockchainInfo.cluster)
            row("Is system chain", blockchainInfo.system)
        }
    })

    // Moving info
    val isMoving = blockchainInfo.isMoving == true && apiVersion >= 33
    if (isMoving) {
        val movingInfo = client.getMovingBlockchainInfo(blockchainRid)
        movingInfo?.let { info ->
            echo(pmcTable {
                captionTop("Moving blockchain info:", TextAlign.LEFT)
                body {
                    row("Source container", info.sourceContainer)
                    row("Destination container", info.destinationContainer)
                    row("Final height", info.finalHeight)
                }
            })

            showHeightsOnClusterNodes(client, movingInfo.sourceContainer, blockchainRid, "Heights on source nodes:")
            showHeightsOnClusterNodes(client, movingInfo.destinationContainer, blockchainRid, "Heights on destination nodes:")
        }
    }

    // Migrating info
    if (blockchainInfo.isForeignImporting == true) {
        when {
            apiVersion >= 33 -> {
                client.getImportingForeignBlockchainInfo(blockchainRid)?.let { info ->
                    echo(pmcTable {
                        captionTop("Importing foreign blockchain info:", TextAlign.LEFT)
                        body {
                            row("Node pubkey", info.pubkey)
                            row("Node host", info.host)
                            row("Node port", info.port)
                            row("Node api-url", info.apiUrl)
                            row("Foreign management chain RID", info.chain0Rid)
                            row("Final height", info.finalHeight)
                        }
                    })
                }
            }

            else -> {
                client.getImportingForeignBlockchainInfoV33(blockchainRid)?.let { info ->
                    echo(pmcTable {
                        captionTop("Importing foreign blockchain info:", TextAlign.LEFT)
                        body {
                            row("Node pubkey", info.pubkey)
                            row("Node host", info.host)
                            row("Node port", info.port)
                            row("Node api-url", info.apiUrl)
                            row("Foreign management chain RID", info.chain0Rid)
                            row("Up to height", info.upToHeight)
                        }
                    })
                }
            }
        }
    }

    // Unarchiving info
    if (blockchainInfo.isUnarchiving == true && apiVersion >= 33) {
        client.getUnarchivingBlockchainInfo(blockchainRid)?.let { info ->
            echo(pmcTable {
                captionTop("Unarchiving blockchain info:", TextAlign.LEFT)
                body {
                    row("Source container", info.sourceContainer)
                    row("Destination container", info.destinationContainer)
                    row("Final height", info.finalHeight)
                }
            })
        }
    }

    // Heights on nodes including anchored height
    if (blockchainInfo.container != null && !isMoving) {
        showHeightsOnClusterNodes(client, blockchainInfo.container, blockchainRid, "Heights on nodes:")
    }

    // Heights on replicas
    val blockchainReplicas = client.getBlockchainReplicas(blockchainRid)
    if (blockchainReplicas.isNotEmpty()) {
        val blockHeightClient = BlockHeightClient(client)
        echo(pmcTable(
                "Heights from replicas",
                listOf("Node", "Height"),
                blockchainReplicas
                        .map { PubKey(it[0].asByteArray()) }
                        .map { CmPeerInfo(it.wData, client.getNodeData(it).apiUrl) }
                        .map { listOf(PubKey(it.pubkey).toShortHex(), blockHeightClient.getCurrentBlockHeightOnPeer(it, blockchainRid).toString()) }
        ))
    }

}

internal fun CliktCommand.showHeightsOnClusterNodes(client: PostchainClient, container: String, blockchainRid: BlockchainRid, caption: String) {
    val cluster = client.getContainerData(container).cluster
    val clusterInfo = client.cmGetClusterInfo(cluster)
    val clusterEndpoints = clusterInfo.peers.map { Endpoint.sanitizeUrl(it.apiUrl) }.let { EndpointPool.default(it) }
    val anchoringChain = when (blockchainRid.wData) {
        client.cmGetSystemAnchoringChain()?.wrap() -> null
        clusterInfo.anchoringChain -> client.cmGetSystemAnchoringChain()?.wrap()
        else -> clusterInfo.anchoringChain
    }
    val blockHeightClient = BlockHeightClient(client)
    val anchoredHeight = blockHeightClient.getLastAnchoredBlockHeight(anchoringChain, clusterEndpoints, blockchainRid)

    echo(pmcTable {
        captionTop(caption, TextAlign.LEFT)
        body {
            row("Anchored height", anchoredHeight)
            clusterInfo.peers.parallelStream()
                    .map { peer -> Pair(peer.pubkey, blockHeightClient.getCurrentBlockHeightOnPeer(peer, blockchainRid, container)) }
                    .toList()
                    .forEach { peerHeight -> row(PubKey(peerHeight.first).toShortHex(), peerHeight.second) }
        }
    })
}
