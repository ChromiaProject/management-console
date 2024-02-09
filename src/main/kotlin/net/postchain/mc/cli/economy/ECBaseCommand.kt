package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.client.core.PostchainClient
import net.postchain.mc.cli.util.pmcConfigOption

abstract class ECBaseCommand(name: String, help: String) : CliktCommand(name = name, help = help) {

    protected val config by pmcConfigOption()
    private val client get() = config.client

    override fun run() {

        val economyChainClient = getEconomyChainClient(client, config.config)

        runEC(client, economyChainClient)
    }

    abstract fun runEC(client: PostchainClient, economyChainClient: PostchainClient)
}