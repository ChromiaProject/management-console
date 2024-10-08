package net.postchain.mc.cli

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.getProviderByKey
import net.postchain.common.hexStringToByteArray
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.network.Version
import net.postchain.mc.network.requireApiVersion

const val DIRECTORY_CHAIN_PROVIDER_MULTI_KEY = 65

abstract class DCBaseCommand(
        name: String,
        help: String,
        private val requiresVersion: Long? = null,
        override val printHelpOnEmptyArgs: Boolean = true
) : PmcCommand(name = name, help = help) {

    val config by pmcConfigOption()
    val client get() = config.client
    val dcVersion get() = Version(config.client).version

    // Get provider pubkey from (1) config, (2) by looking up based on signer keys or (3) use default/first signer key
    val clientProviderPubkey by lazy {
        config.providerPubkey?.hexStringToByteArray() ?:
        getProviderByClientKeys() ?:
        client.pubkey
    }

    override fun run() {

        requiresVersion?.apply {
            client.requireApiVersion(requiresVersion)
        }

        runDC()
    }

    abstract fun runDC()

    fun getProviderByClientKeys(): ByteArray? {
        config.config.signers.forEach {
            client.getProviderByKey(it.pubKey)?.let { provider ->
                return provider
            }
        }
        return null
    }
}