package net.postchain.mc.network

import net.postchain.chain0.common.queries.NodeInfo
import net.postchain.client.config.PostchainClientConfig
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.client.request.EndpointPool
import net.postchain.common.BlockchainRid
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.time.Duration.Companion.seconds

class NodeVerifier(private val configTemplate: PostchainClientConfig, private val sacBrid: BlockchainRid?) {

    private val connectTimeout = 5.seconds

    fun verifyHost(node: NodeInfo) {
        verifyHost(node.host, node.port.toInt())
    }

    fun verifyHost(host: String, port: Int) {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(host, port), connectTimeout.inWholeMilliseconds.toInt())
        }
    }

    fun verifyApi(node: NodeInfo) = verifyApi(node.apiUrl)

    fun verifyApi(url: String = configTemplate.endpointPool.first().url): NodeApiStatus {
        val managementChainStatus = verifyBlockchain(configTemplate.blockchainRid, url)
        val systemAnchorStatus = verifyBlockchain(sacBrid, url)

        when {
            managementChainStatus.third != null -> System.err.println("Node verification failed for management chain ($url): " + managementChainStatus.third?.message)
            systemAnchorStatus.third != null -> System.err.println("Node verification failed for system anchoring chain ($url): " + systemAnchorStatus.third?.message)
        }

        return NodeApiStatus(managementChainStatus.first && systemAnchorStatus.first, managementChainStatus.second, systemAnchorStatus.second)
    }

    fun verifyBlockchain(blockchainRid: BlockchainRid?, url: String): Triple<Boolean, Long?, Exception?> {
        if (blockchainRid == null) return Triple(true, null, null)
        return try {
            val nodeClient = PostchainClientImpl(configTemplate.copy(
                    endpointPool = EndpointPool.singleUrl(url),
                    blockchainRid = blockchainRid
            ))
            Triple(true, nodeClient.currentBlockHeight(), null)
        } catch (e: Exception) {
            Triple(false, null, e)
        }
    }

    data class NodeApiStatus(val responds: Boolean, val height: Long?, val systemAnchorHeight: Long?)
}
