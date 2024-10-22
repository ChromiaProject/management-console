package net.postchain.mc.cli.node

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.nm_api.nmComputeBlockchainList
import net.postchain.common.toHex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pubkeyOption


class CommandListBlockchainsForNode : PmcCommand(
        name = "blockchains",
        help = "List blockchains for node",
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val key by pubkeyOption()

    override fun run() {
        val listBlockchains = client.nmComputeBlockchainList(key.data)
        listBlockchains.forEach { blockchain ->
            echo(blockchain.toHex())
        }
    }
}