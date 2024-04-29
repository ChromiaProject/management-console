package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.client.core.PostchainClient
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.network.Version

abstract class ECBaseCommand(
        name: String,
        help: String,
        private val requiresECVersion: Long? = null,
) : CliktCommand(name = name, help = help) {

    protected val config by pmcConfigOption()
    private val client get() = config.client

    override fun run() {

        val economyChainClient = getEconomyChainClient(client, config.config)

        if (requiresECVersion != null) {
            val ecVersion = Version(economyChainClient)
            if (ecVersion.version < requiresECVersion) {
                throw PrintMessage("Command not supported by economy chain version. Requires version $requiresECVersion but is ${ecVersion.version}")
            }
        }

        runEC(client, economyChainClient)
    }

    abstract fun runEC(client: PostchainClient, economyChainClient: PostchainClient)
}