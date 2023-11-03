package net.postchain.mc.cli.provider

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.table.ColumnWidth
import net.postchain.chain0.common.queries.getAllProviders
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.base.PUBKEY_LENGTH
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption

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
        if (providers.isEmpty()) {
            echo("No providers")
        } else {
            echo("Providers:")
            echo(defaultTable {
                if (interactive) {
                    column(0) {
                        width = ColumnWidth.Fixed(3)
                    }
                } else {
                    column(2) {
                        width = ColumnWidth.Fixed(PUBKEY_LENGTH + 2)
                    }
                }
                header { rowFrom(if (interactive) listOf("#") + headers else headers) }
                body {
                    providers.forEachIndexed { index, it ->
                        val columns = listOf(it.name, it.url, it.pubkey.toHex(), it.system.toString(), it.tier.toString(), it.active.toString())
                        rowFrom(if (interactive) listOf(index.toString()) + columns else columns)
                    }
                }
            })
            if (interactive) {
                promptForIndex(providers)?.let {
                    showProviderInfo(client, PubKey(providers[it].pubkey))
                }
            }
        }
    }
}
