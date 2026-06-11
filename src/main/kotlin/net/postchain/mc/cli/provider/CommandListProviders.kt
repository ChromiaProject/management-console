package net.postchain.mc.cli.provider

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.mordant.input.interactiveSelectList
import net.postchain.chain0.common.queries.getAllProviders
import net.postchain.chain0.model.ProviderTier
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.base.PUBKEY_LENGTH
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandListProviders : PmcCommand(
        name = "list",
        help = "List all providers, optionally filtered by system flag and tier",
        printHelpOnEmptyArgs = false
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()
    private val system by option(help = "Only list system or non-system providers").switch(
            "--system" to true,
            "--non-system" to false
    )
    private val tier by option("--tier", help = "Only list providers of the given tier").enum<ProviderTier>()

    private val headers = listOf("Name", "Url", "Pubkey", "Is System", "Tier", "Active")

    override fun run() {
        val providers = getAllProviders()
        if (interactive && providers.isNotEmpty() && providers.size < terminal.size.height) {
            terminal.interactiveSelectList(providers.map {
                "${it[2]} - ${it[0]}"
            }, "Select provider")?.let {
                showProviderInfo(client, PubKey(it.split(' ').first()))
            }
        } else {
            echo(pmcTable(
                    "providers",
                    headers,
                    providers,
                    2 to PUBKEY_LENGTH,
                    interactive
            ))
            if (interactive && providers.isNotEmpty()) {
                promptForIndex(providers)?.let {
                    showProviderInfo(client, PubKey(providers[it][2]))
                }
            }
        }
    }

    private fun getAllProviders(): List<List<String>> {
        val providers = client.getAllProviders()
                .filter { system == null || it.system == system }
                .filter { tier == null || it.tier == tier }
        return providers.map {
            listOf(it.name, it.url, it.pubkey.toHex(), it.system.toString(), it.tier.toString(), it.active.toString())
        }
    }
}
