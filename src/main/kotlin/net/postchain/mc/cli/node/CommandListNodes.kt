package net.postchain.mc.cli.node

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getAllNodes
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.base.PUBKEY_LENGTH
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandListNodes : PmcCommand(
        name = "list",
        help = "List all nodes",
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()

    private val headers = listOf("Pubkey", "Host", "Port", "REST API", "Territory", "Active", "Provided by")

    override fun run() {
        val nodes = client.getAllNodes(includeInactive = true)
        echo(pmcTable(
                "nodes",
                headers,
                nodes.map {
                    listOf(
                            it.info.pubkey.toHex(),
                            it.info.host,
                            it.info.port.toString(),
                            it.info.apiUrl,
                            it.info.territory ?: "",
                            it.active.toString(),
                            it.provider.pubkey.toHex())
                },
                0 to PUBKEY_LENGTH,
                interactive
        ))
        if (interactive && nodes.isNotEmpty()) {
            promptForIndex(nodes)?.let {
                showNodeInfo(client, PubKey(nodes[it].info.pubkey))
            }
        }
    }
}
