package net.postchain.mc.cli

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.getProviderByKey
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcKeyConfigOption
import net.postchain.mc.network.Version
import net.postchain.mc.network.requireApiVersion

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

    // Get provider pubkey from (1) config, (2) by looking up based on signer keys or (3) use default/first signer key
    val clientProviderPubkey by lazy {
        config.providerPubkey?.data
                ?: getProviderByClientKeys()
                ?: client.pubkey
    }

    override fun run() {

        requiresVersion?.apply {
            client.requireApiVersion(requiresVersion)
        }

        runDC()
    }

    abstract fun runDC()

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
}