package net.postchain.mc.cli.node

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getNodesWithProvider
import net.postchain.mc.cli.util.pmcConfigOption

class CommandListNodes : CliktCommand(
        name = "list",
        help = "List all nodes"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    override fun run() {
        val nodes = client.getNodesWithProvider()
        if (nodes.isEmpty()) {
            echo("No nodes")
        } else {
            echo(defaultTable {
                header { row("Pubkey", "Host", "Port", "Active", "Provided by") }
                body {
                    nodes.forEach {
                        row(it.pubkey.toHex(), it.host, it.port.toString(), it.nodeActive.toString(), it.provider.toHex())
                    }
                }
            })
        }
    }
}