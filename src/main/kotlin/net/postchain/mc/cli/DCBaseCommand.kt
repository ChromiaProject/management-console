package net.postchain.mc.cli

import com.chromia.cli.tools.util.timebOptions
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.cm_api.cmGetSystemAnchoringChain
import net.postchain.chain0.common.queries.getProviderByKey
import net.postchain.chain0.economy_chain_in_directory_chain.getEconomyChainRid
import net.postchain.chain0.token_chain_in_directory_chain.getTokenChainRid
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcKeyConfigOption
import net.postchain.mc.network.Version
import net.postchain.mc.network.requireApiVersion
import java.time.Clock

const val DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION = 65L

/**
 * Use this for commands which makes transactions, or otherwise need access to keys.
 * Commands which only makes queries should inherit from `PmcCommand` instead.
 */
abstract class DCBaseCommand(
        name: String,
        help: String,
        private val requiresVersion: Long? = null,
        override val printHelpOnEmptyArgs: Boolean = true
) : PmcCommand(name = name, help = help) {

    val config by pmcKeyConfigOption()
    val client get() = config.txClient
    val dcVersion get() = Version(client).version

    protected val timeb by timebOptions(Clock.systemUTC())

    // Get provider pubkey from (1) config, (2) by looking up based on signer keys or (3) use default/first signer key
    val clientProviderPubkey by lazy {
        config.providerPubkey?.data
                ?: getProviderByClientKeys()
                ?: client.pubkey
    }

    override fun run() {
        requiresVersion?.apply {
            client.requireApiVersion(dcVersion, requiresVersion)
        }

        runDC()
    }

    abstract fun runDC()

    fun transactionBuilder() = client.transactionBuilder()
            .apply {
                if (timeb != null) {
                    addTimeBound(0, timeb)
                }
            }

    fun getProviderByClientKeys(): ByteArray? {
        if (dcVersion >= 65) {
            config.config.signers.forEach {
                client.getProviderByKey(it.pubKey)?.let { provider ->
                    return provider
                }
            }
        }
        return null
    }

    fun resolveBlockchainRid(systemBlockchain: SystemBlockchain): BlockchainRid = when (systemBlockchain) {
        SystemBlockchain.chain0 -> client.config.blockchainRid
        SystemBlockchain.economy_chain -> BlockchainRid(client.getEconomyChainRid()
                ?: throw CliktError("Economy chain is not installed"))

        SystemBlockchain.token_chain -> {
            val brid = client.getTokenChainRid()
            if (brid.isEmpty()) throw CliktError("Token chain is not installed")
            BlockchainRid(brid)
        }

        SystemBlockchain.system_anchoring_chain -> BlockchainRid(client.cmGetSystemAnchoringChain()
                ?: throw CliktError("System anchoring chain is not installed"))
    }
}
