package net.postchain.mc.cli.provider

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.table.ColumnWidth
import net.postchain.chain0.common.queries.getNodesByProvider
import net.postchain.mc.cli.base.PUBKEY_LENGTH
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.optionalPubkeyOption
import net.postchain.mc.cli.util.pmcConfigOption
import java.time.Instant
import java.util.Date

class CommandListProviderNodes : CliktCommand(
        name = "nodes",
        help = "List nodes by provider"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val pubkey by optionalPubkeyOption()

    override fun run() {
        val providerPubkey = pubkey ?: client.config.pubkey()
        val nodes = client.getNodesByProvider(providerPubkey)
        if (nodes.isEmpty()) {
            echo("No nodes for provider $providerPubkey")
        } else {
            echo("Nodes for provider $providerPubkey")
            echo(defaultTable {
                column(0) {
                    width = ColumnWidth.Fixed(PUBKEY_LENGTH + 2)
                }
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
