package net.postchain.mc.cli.economy

import net.postchain.client.core.PostchainClient
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.network.Version
import net.postchain.mc.network.requireApiVersion

abstract class ECBaseCommand(
        name: String,
        help: String,
        private val requiresECVersion: Long? = null,
        override val printHelpOnEmptyArgs: Boolean = true
) : DCBaseCommand(name = name, help = help) {

    protected lateinit var ecVersion: Version

    override fun runDC() {

        val economyChainClient = getEconomyChainClient(config)
        ecVersion = Version(economyChainClient)

        requiresECVersion?.let {
            economyChainClient.requireApiVersion(requiresECVersion)
        }

        runEC(client, economyChainClient)
    }

    abstract fun runEC(client: PostchainClient, economyChainClient: PostchainClient)
}