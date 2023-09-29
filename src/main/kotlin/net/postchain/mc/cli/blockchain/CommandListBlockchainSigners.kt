package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.theme
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getBlockchainSigners
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.includeInactiveOption
import net.postchain.mc.cli.util.NodeListFormatter.renderNodes
import net.postchain.mc.cli.util.pmcConfigOption


class CommandListBlockchainSigners : CliktCommand(
        name = "signers",
        help = "List blockchain signers"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val includeInactive by includeInactiveOption()

    override fun run() {
        val signers = client.getBlockchainSigners(blockchainRID)
        if (signers.isEmpty()) {
            echo("No signers")
        } else {
            echo(renderNodes(currentContext.theme, signers, includeInactive))
        }

    }
}