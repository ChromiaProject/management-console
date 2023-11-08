package net.postchain.mc.cli.node

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.table.ColumnWidth
import net.postchain.chain0.common.queries.getAllNodes
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.base.PUBKEY_LENGTH
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption

class CommandListNodes : CliktCommand(
        name = "list",
        help = "List all nodes"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()

    private val headers = listOf("Pubkey", "Host", "Port", "REST API", "Territory", "Active", "Provided by")

    override fun run() {
        val nodes = client.getAllNodes(includeInactive = true)
        if (nodes.isEmpty()) {
            echo("No nodes")
        } else {
            echo(defaultTable {
                column(0) {
                    width = ColumnWidth.Fixed(if (interactive) 3 else PUBKEY_LENGTH + 2)
                }
                header { rowFrom(if (interactive) listOf("#") + headers else headers) }
                body {
                    nodes.forEachIndexed { index, it ->
                        val columns = listOf(
                                it.info.pubkey.toHex(),
                                it.info.host,
                                it.info.port.toString(),
                                it.info.apiUrl,
                                it.info.territory,
                                it.active.toString(),
                                it.provider.pubkey.toHex())
                        rowFrom(if (interactive) listOf(index.toString()) + columns else columns)
                    }
                }
            })
            if (interactive) {
                promptForIndex(nodes)?.let {
                    showNodeInfo(client, PubKey(nodes[it].info.pubkey))
                }
            }
        }
    }
}