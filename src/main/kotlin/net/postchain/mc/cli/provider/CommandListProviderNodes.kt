package net.postchain.mc.cli.provider

import net.postchain.chain0.common.queries.getNodesByProvider
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.PUBKEY_LENGTH
import net.postchain.mc.cli.util.optionalPubkeyOption
import net.postchain.mc.cli.util.pmcTable
import java.time.Instant
import java.util.Date

class CommandListProviderNodes : DCBaseCommand(
        name = "nodes",
        help = "List nodes by provider",
        printHelpOnEmptyArgs = false
) {
    private val pubkey by optionalPubkeyOption()

    override fun runDC() {
        val providerPubkey = pubkey ?: PubKey(clientProviderPubkey)
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
