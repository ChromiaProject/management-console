package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getBlockchainSigners
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.includeInactiveOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.renderNodes


class CommandListBlockchainSigners : CliktCommand(
        name = "signers",
        help = "List blockchain signers"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val includeInactive by includeInactiveOption("Include inactive nodes")

    override fun run() {
        val signers = client.getBlockchainSigners(blockchainRID)
        echo(renderNodes("signers", signers, includeInactive))
    }
}