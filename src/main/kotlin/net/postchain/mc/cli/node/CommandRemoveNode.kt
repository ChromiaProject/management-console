package net.postchain.mc.cli.node

import net.postchain.chain0.common.operations.removeNodeOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.pubkeyOption


class CommandRemoveNode : DCBaseCommand(
        name = "remove",
        help = "Removes disabled node"
) {
    private val key by pubkeyOption()

    override fun runDC() {
        client.transactionBuilder()
                .removeNodeOperation(clientProviderPubkey, key.data)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Node removed",
                        "Cannot remove node"
                )
    }
}