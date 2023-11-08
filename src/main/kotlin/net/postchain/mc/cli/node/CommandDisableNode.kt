package net.postchain.mc.cli.node

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.operations.disableNodeOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pubkeyOption


class CommandDisableNode : CliktCommand(
        name = "disable",
        help = "Disables node and removes it from clusters, cluster replicas, blockchain replicas"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val key by pubkeyOption()

    override fun run() {
        client.transactionBuilder()
                .disableNodeOperation(client.config.pubkey().data, key.data)
                .postAwaitConfirmation()
                .printResult(
                        "Node disabled",
                        "Cannot disable node"
                )
    }
}