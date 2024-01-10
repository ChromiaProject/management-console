package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.mordant.rendering.TextAlign
import net.postchain.chain0.cm_api.cmGetClusterInfo
import net.postchain.chain0.cm_api.cmGetSystemAnchoringChain
import net.postchain.chain0.common.queries.getBlockchainInfo
import net.postchain.chain0.common.queries.getImportingForeignBlockchainInfo
import net.postchain.chain0.common.queries.getMovingBlockchainInfo
import net.postchain.chain0.common.queries.getUnarchivingBlockchainInfo
import net.postchain.chain0.version.apiVersion
import net.postchain.client.core.PostchainClient
import net.postchain.client.request.EndpointPool
import net.postchain.common.BlockchainRid
import net.postchain.common.wrap
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.BlockHeightClient
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

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
            showBlockchainInfo(client, blockchainRID)
        } else {
            throw CliktError("blockchain info requires directory chain version 17, found version $apiVersion")
        }
    }
}

fun CliktCommand.showBlockchainInfo(client: PostchainClient, blockchainRid: BlockchainRid) {
    val blockchainInfo = client.getBlockchainInfo(blockchainRid.data)
            ?: throw CliktError("Blockchain with rid $blockchainRid not found")

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

    // Anchored height + heights on nodes
    if (blockchainInfo.cluster != null) {
        val clusterInfo = client.cmGetClusterInfo(blockchainInfo.cluster)
        val clusterEndpoints = clusterInfo.peers.map { it.apiUrl }.let { EndpointPool.default(it) }
        val anchoringChain = when (blockchainInfo.rid) {
            client.cmGetSystemAnchoringChain()?.wrap() -> null
            clusterInfo.anchoringChain -> client.cmGetSystemAnchoringChain()?.wrap()
            else -> clusterInfo.anchoringChain
        }
        val blockHeightClient = BlockHeightClient(client)
        val anchoredHeight = blockHeightClient.getLastAnchoredBlockHeight(anchoringChain, clusterEndpoints, blockchainRid)

        echo(pmcTable {
            captionTop("Heights on nodes:", TextAlign.LEFT)
            body {
                row("Anchored height", anchoredHeight)
                clusterInfo.peers.forEach { peer ->
                    row(PubKey(peer.pubkey).toShortHex(), blockHeightClient.getCurrentBlockHeightOnPeer(peer, blockchainRid))
                }
            }
        })
    }

    // Migrating Blockchain Info
    if (blockchainInfo.isForeignImporting == true) {
        client.getImportingForeignBlockchainInfo(blockchainRid)?.let { info ->
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

    if (blockchainInfo.isMoving == true) {
        client.getMovingBlockchainInfo(blockchainRid)?.let { info ->
            echo(pmcTable {
                captionTop("Moving blockchain info:", TextAlign.LEFT)
                body {
                    row("Source container", info.sourceContainer)
                    row("Destination container", info.destinationContainer)
                    row("Finish at height", info.finishAtHeight)
                }
            })
        }
    }

    if (blockchainInfo.isUnarchiving == true) {
        client.getUnarchivingBlockchainInfo(blockchainRid)?.let { info ->
            echo(pmcTable {
                captionTop("Unarchiving blockchain info:", TextAlign.LEFT)
                body {
                    row("Source container", info.sourceContainer)
                    row("Destination container", info.destinationContainer)
                    row("Finish at height", info.finishAtHeight)
                }
            })
        }
    }
}
