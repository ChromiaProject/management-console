package net.postchain.mc.cli.provider

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.table.ColumnWidth
import net.postchain.chain0.common.queries.getAllProviders
import net.postchain.mc.cli.base.PUBKEY_LENGTH
import net.postchain.mc.cli.util.pmcConfigOption

class CommandListProviders : CliktCommand(
        name = "list",
        help = "List all providers"
) {
    private val config by pmcConfigOption()
    val client get() = config.client

    override fun run() {
        val providers = client.getAllProviders()
        if (providers.isEmpty()) {
            echo("No providers")
        } else {
            echo("Providers:")
            echo(defaultTable {
                column(2) {
                    width = ColumnWidth.Fixed(PUBKEY_LENGTH + 1)
                }
                header { row("Name", "Url", "Pubkey", "Is System", "Tier", "Active") }
                body {
                    providers.forEach {
                        row(it.name, it.url, it.pubkey.toHex(), it.system.toString(), it.tier.toString(), it.active.toString())
                    }
                }
            })
        }
    }
}
