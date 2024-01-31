package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.network.Version


class CommandVersion : CliktCommand(
        name = "version",
        help = "Shows economy chain version"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    override fun run() {

        val economyChainClient = getEconomyChainClient(client, config.config)

        val version = Version(economyChainClient).version
        echo("Economy chain api version: $version")
    }
}