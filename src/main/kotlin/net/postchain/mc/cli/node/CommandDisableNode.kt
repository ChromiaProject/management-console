package net.postchain.mc.cli.node

import net.postchain.chain0.common.operations.disableNodeOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.pubkeyOption


class CommandDisableNode : DCBaseCommand(
        name = "disable",
        help = "Disables node and removes it from clusters, cluster replicas, blockchain replicas"
) {
    private val key by pubkeyOption()

    override fun runDC() {
        transactionBuilder()
                .disableNodeOperation(clientProviderPubkey, key.data)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Node disabled",
                        "Cannot disable node"
                )
    }
}