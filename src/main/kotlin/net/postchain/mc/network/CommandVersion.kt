package net.postchain.mc.network

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.mc.cli.util.pmcConfigOption


class CommandVersion : PmcCommand(
        name = "version",
        help = "Shows network version"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    override fun run() {
        val version = Version(client).version
        echo("Directory1 api version: $version")
    }
}