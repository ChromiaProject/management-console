package net.postchain.mc.cli.provider

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import net.postchain.chain0.common.queries.getNodesByProvider
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcConfigOption

import java.time.Instant
import java.util.Date

class CommandListProviderNodes : CliktCommand(
        name = "nodes",
        help = "List nodes by provider"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val key by option("-pk", "--pubkey").convert { PubKey(it) }.defaultLazy { client.config.pubkey() }

    override fun run() {
        val nodes = client.getNodesByProvider(key)
        if (nodes.isEmpty()) {
            echo("No nodes for provider $key")
        } else {
            echo("Nodes for provider $key")
            echo(defaultTable {
                header { row("Pubkey", "Host", "Port", "REST API", "Territory", "Active", "Last updated") }
                body {
                    nodes.forEach {
                        row(
                                it.pubkey.toHex(),
                                it.host,
                                it.port.toString(),
                                it.apiUrl,
                                it.territory,
                                it.active.toString(),
                                Date.from(Instant.ofEpochMilli(it.lastUpdated)).toString()
                        )
                    }
                }
            })
        }
    }
}
