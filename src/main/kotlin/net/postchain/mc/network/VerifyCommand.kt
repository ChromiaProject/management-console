package net.postchain.mc.network

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import net.postchain.chain0.cm_api.cmGetSystemAnchoringChain
import net.postchain.chain0.common.queries.getAllNodes
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class VerifyCommand : CliktCommand(help = "Verify that all nodes are accessible") {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val showProgress by option(help = "Show which node is currently being verified").flag()

    override fun run() {
        client.requireApiVersion(2)
        val nodeVerifier = NodeVerifier(client.config, client.cmGetSystemAnchoringChain()?.let { BlockchainRid(it) })
        echo(pmcTable(
                "nodes",
                listOf("Node public key", "Node host", "Node provider", "Network", "Api", "Management chain", "System anchoring"),
                client.getAllNodes(false).map { node ->
                    if (showProgress and terminal.info.outputInteractive) echo("Verifying node: ${node.info.apiUrl}, ${node.info.pubkey}")
                    val (apiAccessible, height, sacHeight) = nodeVerifier.verifyApi(node.info)
                    val hostResponds = nodeVerifier.verifyHost(node.info)
                    listOf(
                            node.info.pubkey.toHex(),
                            node.info.host,
                            "${node.provider.pubkey.toHex()}${if (node.provider.name.isNotEmpty()) " - ${node.provider.name}" else ""}",
                            hostResponds.isOk(),
                            apiAccessible.isOk(),
                            "$height",
                            "$sacHeight"
                    )
                }
        ))
    }

    private fun Boolean?.isOk(): String = this?.let { if (this) "OK" else "Bad" } ?: "Bad"

}
