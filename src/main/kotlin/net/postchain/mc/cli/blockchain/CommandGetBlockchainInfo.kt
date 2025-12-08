package net.postchain.mc.cli.blockchain

import com.chromia.cli.base.formatter.jsonTable
import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextAlign
import net.postchain.chain0.cm_api.CmPeerInfo
import net.postchain.chain0.cm_api.cmGetClusterInfo
import net.postchain.chain0.cm_api.cmGetSystemAnchoringChain
import net.postchain.chain0.common.queries.BlockchainInfo
import net.postchain.chain0.common.queries.getBlockchainInfo
import net.postchain.chain0.common.queries.getBlockchainInfoByName
import net.postchain.chain0.common.queries.getBlockchainReplicas
import net.postchain.chain0.common.queries.getContainerData
import net.postchain.chain0.common.queries.getImportingForeignBlockchainInfo
import net.postchain.chain0.common.queries.getMovingBlockchainInfo
import net.postchain.chain0.common.queries.getNodeData
import net.postchain.chain0.common.queries.getUnarchivingBlockchainInfo
import net.postchain.chain0.version.apiVersion
import net.postchain.client.core.PostchainReadClient
import net.postchain.client.request.Endpoint
import net.postchain.client.request.EndpointPool
import net.postchain.common.BlockchainRid
import net.postchain.common.wrap
import net.postchain.crypto.PubKey
import net.postchain.d1.client.ChromiaClient
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.SystemBlockchain
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.resolveSystemBlockchain
import net.postchain.mc.cli.util.BlockHeightClient
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.cli.util.prettyTable

class CommandGetBlockchainInfo : PmcCommand(
        name = "info",
        help = "Get blockchain info"
) {
    private val config by pmcConfigOption()

    private val blockchain by mutuallyExclusiveOptions(
            blockchainRidOption(),
            option("-chain", "--blockchain-name", help = "Blockchain name", metavar = "NAME"),
    ).required()

    override fun run() {
        val client = config.client
        val apiVersion = client.apiVersion()
        if (apiVersion >= 17) {
            val blockchainInfo = when (val bc = blockchain) {
                is BlockchainRid -> client.getBlockchainInfo(bc.data)
                        ?: throw CliktError("Blockchain with rid $bc not found")

                is String ->
                    getSystemBlockchainOrNull(bc)?.let {
                        client.getBlockchainInfo(resolveSystemBlockchain(config.clientConfig, client, it).data)
                                ?: throw CliktError("Blockchain with rid $bc not found")
                    } ?: if (apiVersion >= 107)
                        client.getBlockchainInfoByName(bc) ?: throw CliktError("Blockchain with name $bc not found")
                    else
                        throw CliktError("blockchain info by name requires directory chain version 107, found version $apiVersion")

                else -> throw CliktError("Unknown error")
            }
            showBlockchainInfo(client, config.chromiaClient, apiVersion, blockchainInfo)
        } else {
            throw CliktError("blockchain info requires directory chain version 17, found version $apiVersion")
        }
    }

    private fun getSystemBlockchainOrNull(bc: String): SystemBlockchain? = try {
        SystemBlockchain.valueOf(bc)
    } catch (_: IllegalArgumentException) {
        null
    }
}

internal fun CliktCommand.showBlockchainInfo(client: PostchainReadClient, chromiaClient: ChromiaClient, apiVersion: Long, blockchainRid: BlockchainRid) {
    val blockchainInfo = client.getBlockchainInfo(blockchainRid.data)
            ?: throw CliktError("Blockchain with rid $blockchainRid not found")

    showBlockchainInfo(client, chromiaClient, apiVersion, blockchainInfo)
}

internal fun CliktCommand.showBlockchainInfo(client: PostchainReadClient, chromiaClient: ChromiaClient, apiVersion: Long, blockchainInfo: BlockchainInfo) {
    val blockchainRid = BlockchainRid(blockchainInfo.rid)

    if (!terminal.terminalInfo.outputInteractive) {
        echo("{")
        echo(""""basic": """, trailingNewline = false)
    }
    echo(pmcTable {
        captionTop("Basic info:", TextAlign.LEFT)
        body {
            row("Name", blockchainInfo.name)
            row("RID", blockchainInfo.rid)
            row("State", blockchainInfo.state)
            row("Container", blockchainInfo.container)
            row("Cluster", blockchainInfo.cluster)
            row("Is system chain", blockchainInfo.system)

            if (apiVersion >= 63) {
                row("Configuration delay", blockchainInfo.configDelay ?: "no")
            }
        }
    })

    // Moving info
    val isMoving = blockchainInfo.isMoving == true && apiVersion >= 33
    if (isMoving) {
        val movingInfo = client.getMovingBlockchainInfo(blockchainRid)
        movingInfo?.let { info ->
            if (!terminal.terminalInfo.outputInteractive) {
                echo(""","moving": """, trailingNewline = false)
            }
            echo(pmcTable {
                captionTop("Moving blockchain info:", TextAlign.LEFT)
                body {
                    row("Source container", info.sourceContainer)
                    row("Destination container", info.destinationContainer)
                    row("Final height", info.finalHeight)
                }
            })

            showHeightsOnClusterNodes(client, chromiaClient, movingInfo.sourceContainer, blockchainRid, "Heights on source nodes:", isMoving)
            showHeightsOnClusterNodes(client, chromiaClient, movingInfo.destinationContainer, blockchainRid, "Heights on destination nodes:", isMoving)
        }
    }

    // Migrating info
    if (blockchainInfo.isForeignImporting == true) {
        if (!terminal.terminalInfo.outputInteractive) {
            echo(""","moving": """, trailingNewline = false)
        }
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

    // Unarchiving info
    if (blockchainInfo.isUnarchiving == true && apiVersion >= 33) {
        client.getUnarchivingBlockchainInfo(blockchainRid)?.let { info ->
            if (!terminal.terminalInfo.outputInteractive) {
                echo(""","unarchiving": """, trailingNewline = false)
            }
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
        showHeightsOnClusterNodes(client, chromiaClient, blockchainInfo.container, blockchainRid, "Heights on nodes:", isMoving)
    }

    // Heights on replicas
    val blockchainReplicas = client.getBlockchainReplicas(blockchainRid)
    if (blockchainReplicas.isNotEmpty()) {
        val blockHeightClient = BlockHeightClient(chromiaClient)
        if (!terminal.terminalInfo.outputInteractive) {
            echo(""","replica_heights": """, trailingNewline = false)
        }
        echo(if (terminal.terminalInfo.outputInteractive) prettyTable(
                "Heights from replicas",
                listOf("Pubkey", "API URL", "Height"),
                blockchainReplicas
                        .map { PubKey(it[0].asByteArray()) }
                        .map { CmPeerInfo(it.wData, client.getNodeData(it).apiUrl) }
                        .map {
                            val heightOnReplica = blockHeightClient.getCurrentBlockHeightOnPeer(it, blockchainRid)
                            listOf(
                                    PubKey(it.pubkey).hex(),
                                    it.apiUrl,
                                    if (heightOnReplica < 0) "Unknown" else heightOnReplica.toString(),
                            )
                        }
        ) else jsonTable {
            body {
                blockchainReplicas
                        .map { PubKey(it[0].asByteArray()) }
                        .map { CmPeerInfo(it.wData, client.getNodeData(it).apiUrl) }
                        .forEach {
                            val heightOnReplica = blockHeightClient.getCurrentBlockHeightOnPeer(it, blockchainRid)
                            row(
                                    PubKey(it.pubkey).hex(),
                                    if (heightOnReplica < 0) "Unknown" else heightOnReplica.toString(),
                            )
                        }
            }
        })
    }

    if (!terminal.terminalInfo.outputInteractive) {
        echo("}")
    }
}

internal fun CliktCommand.showHeightsOnClusterNodes(client: PostchainReadClient, chromiaClient: ChromiaClient, container: String, blockchainRid: BlockchainRid, caption: String, isMoving: Boolean) {
    val cluster = client.getContainerData(container).cluster
    val clusterInfo = client.cmGetClusterInfo(cluster)
    val clusterEndpoints = clusterInfo.peers.map { Endpoint.sanitizeUrl(it.apiUrl) }.let { EndpointPool.default(it) }
    val anchoringChain = when (blockchainRid.wData) {
        client.cmGetSystemAnchoringChain()?.wrap() -> null
        clusterInfo.anchoringChain -> client.cmGetSystemAnchoringChain()?.wrap()
        else -> clusterInfo.anchoringChain
    }
    val blockHeightClient = BlockHeightClient(chromiaClient)
    val anchoredHeight = blockHeightClient.getLastAnchoredBlockHeight(anchoringChain, clusterEndpoints, blockchainRid)
    val nodeHeights = clusterInfo.peers.parallelStream()
            .map { peer -> peer to blockHeightClient.getCurrentBlockHeightOnPeer(peer, blockchainRid, if (isMoving) container else null) }
            .toList()

    if (!terminal.terminalInfo.outputInteractive) {
        echo(""","heights": """, trailingNewline = false)
    }
    echo(if (terminal.terminalInfo.outputInteractive) defaultTable {
        captionTop(caption, TextAlign.LEFT)
        body {
            row("Anchored height", "", anchoredHeight)
            nodeHeights
                    .forEach { (peer, height) -> row(PubKey(peer.pubkey).hex(), peer.apiUrl, height) }
        }
    } else jsonTable {
        body {
            row("Anchored_height", anchoredHeight)
            nodeHeights.forEach { (peer, height) ->
                row(PubKey(peer.pubkey).hex(), height)
            }
        }
    })
}
