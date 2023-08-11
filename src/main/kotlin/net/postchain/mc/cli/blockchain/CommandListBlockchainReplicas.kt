package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getBlockchainReplicas
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.includeInactiveOption
import net.postchain.mc.cli.util.NodeListFormatter.renderNodes
import net.postchain.mc.cli.util.pmcConfigOption


class CommandListBlockchainReplicas : CliktCommand(
        name = "replicas",
        help = "List blockchain replicas"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val includeInactive by includeInactiveOption()

    override fun run() {
        val replicas = client.getBlockchainReplicas(blockchainRID)
        if (replicas.isEmpty()) {
            echo("No replicas")
        } else {
            renderNodes(replicas, includeInactive).also { echo(it) }
        }
    }
}