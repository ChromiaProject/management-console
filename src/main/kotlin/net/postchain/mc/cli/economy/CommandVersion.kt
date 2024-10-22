package net.postchain.mc.cli.economy

import net.postchain.client.core.PostchainClient
import net.postchain.mc.network.Version


class CommandVersion : ECBaseCommand(
        name = "version",
        help = "Shows economy chain version",
        printHelpOnEmptyArgs = false
) {
    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        val version = Version(economyChainClient).version
        echo("Economy chain api version: $version")
    }
}