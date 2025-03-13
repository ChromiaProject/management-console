package net.postchain.mc.cli.node

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.portOption
import net.postchain.mc.cli.requiredHostOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.network.NodeVerifier
import java.io.IOException

class CommandPingNode : PmcCommand(
        name = "ping",
        help = "Check if a node is accessible"
) {
    private val config by pmcConfigOption()

    private val host by requiredHostOption()

    private val port by portOption().required()

    override fun run() {
        val verifier = NodeVerifier(config.chromiaClient.config, null)
        try {
            verifier.verifyHost(host, port)
            echo("Node is accessible")
        } catch (e: IOException) {
            throw CliktError("Node is not accessible: $e")
        }
    }
}
