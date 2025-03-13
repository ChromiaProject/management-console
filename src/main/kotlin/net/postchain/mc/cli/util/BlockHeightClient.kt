package net.postchain.mc.cli.util

import net.postchain.anchoring.anchoring_chain_common.getLastAnchoredBlock
import net.postchain.chain0.cm_api.CmPeerInfo
import net.postchain.client.config.RequestStrategies
import net.postchain.client.exception.ClientError
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.client.request.EndpointPool
import net.postchain.client.request.SingleEndpointPool
import net.postchain.common.BlockchainRid
import net.postchain.common.types.WrappedByteArray
import net.postchain.d1.client.ChromiaClient
import java.time.Duration

class BlockHeightClient(private val chromiaClient: ChromiaClient) {

    fun getLastAnchoredBlockHeight(
            anchoringChain: WrappedByteArray?,
            clusterEndpoints: EndpointPool,
            blockchainRid: BlockchainRid,
            connectTimeout: Duration = Duration.ofMillis(1000),
            responseTimeout: Duration = Duration.ofMillis(1000)
    ): Long =
            try {
                if (anchoringChain == null) {
                    -1
                } else {
                    PostchainClientImpl(chromiaClient.config.copy(
                            blockchainRid = BlockchainRid(anchoringChain),
                            endpointPool = clusterEndpoints,
                            connectTimeout = connectTimeout,
                            responseTimeout = responseTimeout,
                            requestStrategy = RequestStrategies.TRY_NEXT_ON_ERROR.factory
                    )).getLastAnchoredBlock(blockchainRid)?.blockHeight ?: -1
                }
            } catch (_: ClientError) {
                -1
            }

    fun getCurrentBlockHeightOnPeer(
            peer: CmPeerInfo,
            blockchainRid: BlockchainRid,
            container: String? = null,
            connectTimeout: Duration = Duration.ofMillis(1000),
            responseTimeout: Duration = Duration.ofMillis(1000)
    ) = try {
        if (peer.apiUrl.isEmpty()) {
            -1
        } else {
            PostchainClientImpl(chromiaClient.config.copy(
                    blockchainRid = blockchainRid,
                    endpointPool = SingleEndpointPool(peer.apiUrl),
                    connectTimeout = connectTimeout,
                    responseTimeout = responseTimeout,
            )).currentBlockHeight(container)
        }
    } catch (_: ClientError) {
        -1
    }
}
