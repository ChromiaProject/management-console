package net.postchain.mc.cli.node

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.operations.removeNodeOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.requiredPubkeyOption
import net.postchain.mc.cli.util.pmcConfigOption


class CommandRemoveNode : CliktCommand(
        name = "remove",
        help = "Removes disabled node"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val key by requiredPubkeyOption()

    override fun run() {
        client.transactionBuilder()
                .removeNodeOperation(client.config.pubkey().data, key.data)
                .postAwaitConfirmation()
                .printResult(
                        "Node removed",
                        "Cannot remove node"
                )
    }
}