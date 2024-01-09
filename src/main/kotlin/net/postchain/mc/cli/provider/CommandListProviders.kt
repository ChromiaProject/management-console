package net.postchain.mc.cli.provider

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getAllProviders
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.base.PUBKEY_LENGTH
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandListProviders : CliktCommand(
        name = "list",
        help = "List all providers"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()

    private val headers = listOf("Name", "Url", "Pubkey", "Is System", "Tier", "Active")

    override fun run() {
        val providers = client.getAllProviders()
        echo(pmcTable(
                "providers",
                headers,
                providers.map {
                    listOf(it.name, it.url, it.pubkey.toHex(), it.system.toString(), it.tier.toString(), it.active.toString())
                },
                2 to PUBKEY_LENGTH,
                interactive
        ))
        if (interactive && providers.isNotEmpty()) {
            promptForIndex(providers)?.let {
                showProviderInfo(client, PubKey(providers[it].pubkey))
            }
        }
    }
}
