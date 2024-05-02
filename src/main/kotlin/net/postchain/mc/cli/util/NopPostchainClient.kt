package net.postchain.mc.cli.util

import com.chromia.build.tools.config.ChromiaClientConfig
import com.chromia.build.tools.config.ChromiaConfigWriter
import com.chromia.cli.tools.blockchain.BridFetcher
import net.postchain.client.config.PostchainClientConfig
import net.postchain.client.core.PostchainClient
import net.postchain.client.defaultHttpHandler
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.client.request.EndpointPool
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.BlockchainRid
import net.postchain.crypto.KeyPair

/**
 * Postchain client that adds a no-op to each [TransactionBuilder]
 */
class NopPostchainClient(val client: PostchainClient) : PostchainClient by client {
    override fun transactionBuilder() = client.transactionBuilder().addNop()
    override fun transactionBuilder(signers: List<KeyPair>) = client.transactionBuilder(signers).addNop()

    companion object {
        fun withCachedBrid(config: ChromiaClientConfig, configuredBrid: String?, lookupBrid: Boolean, useRequestCompression: Boolean): PostchainClient {
            val brid = (if (!lookupBrid)
                configuredBrid
            else
                null)
                    ?.let { BlockchainRid.buildFromHex(it) }
                    ?: findAndCacheBrid(config)
            return NopPostchainClient(config.setBrid(brid).client { conf ->
                PostchainClientImpl(conf.copy(compressRequestBodies = useRequestCompression))
            })
        }

        private fun findAndCacheBrid(config: ChromiaClientConfig): BlockchainRid {
            val brid = BridFetcher(
                    httpHandler = defaultHttpHandler(PostchainClientConfig(BlockchainRid.ZERO_RID, endpointPool = EndpointPool.singleUrl(""))),
                    url = requireNotNull(config.endpointPool.firstOrNull()?.url) { "Missing 'api.url'" })
                    .fetchBlockchainRid(0)
            ChromiaConfigWriter.local.setBrid(brid)
            return brid
        }
    }
}
