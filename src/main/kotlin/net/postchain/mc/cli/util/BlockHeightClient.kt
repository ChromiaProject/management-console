package net.postchain.mc.cli.util

import net.postchain.anchoring.anchoring_chain_common.getLastAnchoredBlock
import net.postchain.chain0.cm_api.CmPeerInfo
import net.postchain.client.config.RequestStrategies
import net.postchain.client.core.PostchainClient
import net.postchain.client.exception.ClientError
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.client.request.EndpointPool
import net.postchain.client.request.SingleEndpointPool
import net.postchain.common.BlockchainRid
import net.postchain.common.types.WrappedByteArray
import java.time.Duration

class BlockHeightClient(
        private val parentClient: PostchainClient,
        private val connectTimeout: Duration = Duration.ofMillis(1000),
        private val responseTimeout: Duration = Duration.ofMillis(1000)
) {

    fun getLastAnchoredBlockHeight(anchoringChain: WrappedByteArray?, clusterEndpoints: EndpointPool, blockchainRid: BlockchainRid): Long =
            try {
                if (anchoringChain == null) {
                    -1
                } else {
                    PostchainClientImpl(parentClient.config.copy(
                            blockchainRid = BlockchainRid(anchoringChain),
                            endpointPool = clusterEndpoints,
                            connectTimeout = connectTimeout,
                            responseTimeout = responseTimeout,
                            requestStrategy = RequestStrategies.TRY_NEXT_ON_ERROR.factory
                    )).getLastAnchoredBlock(blockchainRid)?.blockHeight ?: -1
                }
            } catch (e: ClientError) {
                -1
            }

    fun getCurrentBlockHeightOnPeer(peer: CmPeerInfo, blockchainRid: BlockchainRid) = try {
        PostchainClientImpl(parentClient.config.copy(
                blockchainRid = blockchainRid,
                endpointPool = SingleEndpointPool(peer.apiUrl),
                connectTimeout = connectTimeout,
                responseTimeout = responseTimeout,
        )).currentBlockHeight()
    } catch (e: ClientError) {
        -1
    }
}