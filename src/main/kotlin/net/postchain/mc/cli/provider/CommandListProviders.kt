package net.postchain.mc.cli.provider

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getAllProviders
import net.postchain.chain0.version.apiVersion
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.base.PUBKEY_LENGTH
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV47.getAllProviders47

class CommandListProviders : PmcCommand(
        name = "list",
        help = "List all providers",
        printHelpOnEmptyArgs = false
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()

    private val headers = listOf("Name", "Url", "Pubkey", "Is System", "Tier", "Active")

    override fun run() {
        val providers = getAllProviders()
        echo(pmcTable(
                "providers",
                headers,
                providers,
                2 to PUBKEY_LENGTH,
                interactive
        ))
        if (interactive && providers.isNotEmpty()) {
            promptForIndex(providers)?.let {
                showProviderInfo(client, PubKey(providers[it][3]))
            }
        }
    }

    private fun getAllProviders(): List<List<String>> {
        return if (client.apiVersion() < 47L) {
            val providers = client.getAllProviders47()
            providers.map {
                listOf(it.name, it.url, it.pubkey.toHex(), it.system.toString(), it.tier.toString(), it.active.toString())
            }
        } else {
            val providers = client.getAllProviders()
            providers.map {
                listOf(it.name, it.url, it.pubkey.toHex(), it.system.toString(), it.tier.toString(), it.active.toString())
            }
        }
    }
}
