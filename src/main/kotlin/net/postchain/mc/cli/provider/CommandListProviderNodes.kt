package net.postchain.mc.cli.provider

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getNodesByProvider
import net.postchain.mc.cli.base.PUBKEY_LENGTH
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.optionalPubkeyOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
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
        echo(pmcTable(
                "nodes for provider $providerPubkey",
                listOf("Pubkey", "Host", "Port", "REST API", "Territory", "Active", "Last updated"),
                nodes.map {
                    listOf(
                            it.pubkey.toHex(),
                            it.host,
                            it.port.toString(),
                            it.apiUrl,
                            it.territory ?: "",
                            it.active.toString(),
                            Date.from(Instant.ofEpochMilli(it.lastUpdated)).toString()
                    )
                },
                0 to PUBKEY_LENGTH
        ))
    }
}
