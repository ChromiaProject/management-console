package net.postchain.mc.cli.util

import com.chromia.cli.tools.blockchain.BridFinder
import com.chromia.cli.tools.config.ChromiaConfig
import com.chromia.cli.tools.config.ChromiaConfigLoader
import com.chromia.cli.tools.config.ChromiaConfigWriter
import net.postchain.chain0.cm_api.cmGetBlockchainApiUrls
import net.postchain.client.config.PostchainClientConfig
import net.postchain.client.core.PostchainClient
import net.postchain.client.defaultHttpHandler
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.client.request.EndpointPool
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.BlockchainRid
import net.postchain.common.config.getEnvOrListProperty
import net.postchain.common.config.getEnvOrStringProperty
import net.postchain.crypto.KeyPair
import java.time.Duration

/**
 * Postchain client that adds a no-op to each [TransactionBuilder]
 */
class NopPostchainClient(val client: PostchainClient) : PostchainClient by client {
    override fun transactionBuilder() = client.transactionBuilder().addNop()
    override fun transactionBuilder(signers: List<KeyPair>) = client.transactionBuilder(signers).addNop()

    companion object {
        fun withCaching(config: ChromiaConfig): PostchainClient {
            val brid = config.getEnvOrStringProperty("POSTCHAIN_CLIENT_BLOCKCHAIN_RID", "brid")
                    ?.let { BlockchainRid.buildFromHex(it) }
                    ?: findAndCacheBrid(config)
            val endpointPool = findAndCacheEndpoints(config, brid)
            return NopPostchainClient(PostchainClientImpl(config.get(blockchainRid = brid).copy(endpointPool = endpointPool)))
        }

        private fun findAndCacheEndpoints(config: ChromiaConfig, blockchainRid: BlockchainRid): EndpointPool {
           val apiUrl = config.getEnvOrListProperty("POSTCHAIN_CLIENT_API_URL", "api.url", listOf())
            if (apiUrl.size == 1 || ChromiaConfigLoader.localConfigurationFile().lastModified() + Duration.ofDays(1).toMillis() < System.currentTimeMillis()) {
                val extendedList =
                    PostchainClientImpl(config.get(blockchainRid = blockchainRid)).cmGetBlockchainApiUrls(blockchainRid).toMutableSet()
                extendedList.addAll(apiUrl)
                ChromiaConfigWriter.local.setProperty("api.url" to extendedList.joinToString(","))
                return EndpointPool.default(extendedList.toList())
            }
            return EndpointPool.default(apiUrl)
        }

        private fun findAndCacheBrid(config: ChromiaConfig): BlockchainRid {
            val brid = BridFinder(
                    httpHandler = defaultHttpHandler(PostchainClientConfig(BlockchainRid.ZERO_RID, endpointPool = EndpointPool.singleUrl(""))),
                    url = requireNotNull(config.getEnvOrListProperty("POSTCHAIN_CLIENT_API_URL", "api.url", listOf()).firstOrNull()) { "Missing 'api.url'" })
                    .findBlockchainRid(0)
            ChromiaConfigWriter.local.setBrid(brid)
            return brid
        }
    }
}
