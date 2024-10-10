package net.postchain.mc.cli.blockchain

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getBlockchainReplicas
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.includeInactiveOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.renderNodes


class CommandListBlockchainReplicas : PmcCommand(
        name = "replicas",
        help = "List blockchain replicas"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val includeInactive by includeInactiveOption("Include inactive nodes")

    override fun run() {
        val replicas = client.getBlockchainReplicas(blockchainRID)
        echo(renderNodes("replicas", replicas, includeInactive))
    }
}