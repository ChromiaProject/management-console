package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.groups.required
import net.postchain.chain0.common.queries.getBlockchainReplicas
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.blockchainOption
import net.postchain.mc.cli.includeInactiveOption
import net.postchain.mc.cli.resolveBlockchain
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.renderNodes


class CommandListBlockchainReplicas : PmcCommand(
        name = "replicas",
        help = "List blockchain replicas",
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchain by blockchainOption().required()

    private val includeInactive by includeInactiveOption("Include inactive nodes")

    override fun run() {
        val blockchainRID = resolveBlockchain(config.clientConfig, config.client, blockchain)
        val replicas = client.getBlockchainReplicas(blockchainRID)
        echo(renderNodes("replicas", replicas, includeInactive))
    }
}