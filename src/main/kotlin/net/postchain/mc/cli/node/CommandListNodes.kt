package net.postchain.mc.cli.node

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.table.ColumnWidth
import net.postchain.chain0.common.queries.getAllNodes
import net.postchain.mc.cli.base.PUBKEY_LENGTH
import net.postchain.mc.cli.util.pmcConfigOption

class CommandListNodes : CliktCommand(
        name = "list",
        help = "List all nodes"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    override fun run() {
        val nodes = client.getAllNodes(includeInactive = true)
        if (nodes.isEmpty()) {
            echo("No nodes")
        } else {
            echo(defaultTable {
                column(0) {
                    width = ColumnWidth.Fixed(PUBKEY_LENGTH + 1)
                }
                header { row("Pubkey", "Host", "Port", "REST API", "Territory", "Active", "Provided by") }
                body {
                    nodes.forEach {
                        row(it.info.pubkey.toHex(), it.info.host, it.info.port.toString(), it.info.apiUrl, it.info.territory, it.active.toString(), it.provider.pubkey.toHex())
                    }
                }
            })
        }
    }
}