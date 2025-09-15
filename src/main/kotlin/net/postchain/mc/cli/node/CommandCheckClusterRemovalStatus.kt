package net.postchain.mc.cli.node

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.anchoring.anchoring_chain_common.getLastAnchoredBlock
import net.postchain.chain0.cm_api.cmGetClusterInfo
import net.postchain.chain0.cm_api.cmGetSystemAnchoringChain
import net.postchain.chain0.common.queries.getNodeSignerClusterBlockchains
import net.postchain.chain0.model.BlockchainState
import net.postchain.chain0.model.ContainerState
import net.postchain.common.BlockchainRid
import net.postchain.common.wrap
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.cli.util.pubkeyOption
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CommandCheckClusterRemovalStatus : DCBaseCommand(
        name = "check-cluster-removal-status",
        help = "Checks if a node has been successfully removed from a cluster",
        requiresVersion = 99
) {
    private val key by pubkeyOption("Node pubkey")
    private val cluster by option("-c", "--cluster").required()

    override fun runDC() {
        if (terminal.terminalInfo.outputInteractive) echo("Fetching chains that still has node as signer in latest confirmed configuration...")
        val remainingSignerChains = config.client.getNodeSignerClusterBlockchains(
                key,
                cluster,
                listOf(BlockchainState.RUNNING),
                listOf(ContainerState.RUNNING)
        ).map { it.wrap() }

        if (remainingSignerChains.isEmpty()) {
            if (terminal.terminalInfo.outputInteractive) {
                echo()
                echo("Removal is completed! No running blockchains in cluster '$cluster' have the node as signer anymore.")
            } else {
                echo("[]")
            }
        } else {
            if (terminal.terminalInfo.outputInteractive) echo("Fetching latest anchored heights for remaining chains...")
            val clusterInfo = client.cmGetClusterInfo(cluster)
            val bridsWithAnchoredBlockRows = remainingSignerChains.parallelStream().map { blockchainRid ->
                // Special case, return last block height for SAC
                val lastAnchoredBlockTimestamp = if (client.cmGetSystemAnchoringChain()?.wrap() == blockchainRid) {
                    val sacClient = config.chromiaClient.getSystemAnchoringClient()
                    val lastSacBlockHeight = sacClient.currentBlockHeight() - 1
                    sacClient.blockAtHeight(lastSacBlockHeight)?.timestamp
                } else {
                    val anchoringChain = when (blockchainRid) {
                        clusterInfo.anchoringChain -> client.cmGetSystemAnchoringChain()?.wrap()
                        else -> clusterInfo.anchoringChain
                    }

                    anchoringChain?.let {
                        config.chromiaClient.getClient(BlockchainRid(anchoringChain))
                                .getLastAnchoredBlock(BlockchainRid(blockchainRid))
                    }?.timestamp
                }

                val formattedTime = lastAnchoredBlockTimestamp?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))
                } ?: "Unknown"
                listOf(blockchainRid.toHex(), formattedTime)
            }.toList()

            echo()
            echo(pmcTable(
                    "Remaining chains with node as signer. If last anchored block is a long time ago, the blockchain is probably broken and can be ignored",
                    listOf("Blockchain RID", "Last anchored block time"),
                    bridsWithAnchoredBlockRows
            ))
        }
    }
}
