package net.postchain.mc.cli.util

import com.chromia.cli.tools.blockchain.BridFetcher
import com.chromia.cli.tools.config.ChromiaConfig
import com.chromia.cli.tools.config.ChromiaConfigWriter
import net.postchain.client.config.PostchainClientConfig
import net.postchain.client.core.PostchainClient
import net.postchain.client.defaultHttpHandler
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.client.request.EndpointPool
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.BlockchainRid
import net.postchain.common.config.getEnvOrBooleanProperty
import net.postchain.common.config.getEnvOrStringProperty
import net.postchain.crypto.KeyPair

/**
 * Postchain client that adds a no-op to each [TransactionBuilder]
 */
class NopPostchainClient(val client: PostchainClient) : PostchainClient by client {
    override fun transactionBuilder() = client.transactionBuilder().addNop()
    override fun transactionBuilder(signers: List<KeyPair>) = client.transactionBuilder(signers).addNop()

    companion object {
        fun withCachedBrid(config: ChromiaConfig, lookupBrid: Boolean): PostchainClient {
            val brid = (if (!lookupBrid)
                config.getEnvOrStringProperty("POSTCHAIN_CLIENT_BLOCKCHAIN_RID", "brid")
            else
                null)
                    ?.let { BlockchainRid.buildFromHex(it) }
                    ?: findAndCacheBrid(config)
            val useRequestCompression = config.getEnvOrBooleanProperty("POSTCHAIN_CLIENT_COMPRESS_REQUEST_BODIES", "compress.requests", true)
            return NopPostchainClient(PostchainClientImpl(config.get(blockchainRid = brid).copy(compressRequestBodies = useRequestCompression)))
        }

        private fun findAndCacheBrid(config: ChromiaConfig): BlockchainRid {
            val brid = BridFetcher(
                    httpHandler = defaultHttpHandler(PostchainClientConfig(BlockchainRid.ZERO_RID, endpointPool = EndpointPool.singleUrl(""))),
                    url = requireNotNull(config.getEnvOrStringProperty("POSTCHAIN_CLIENT_API_URL", "api.url")) { "Missing 'api.url'" })
                    .fetchBlockchainRid(0)
            ChromiaConfigWriter.local.setBrid(brid)
            return brid
        }
    }
}
