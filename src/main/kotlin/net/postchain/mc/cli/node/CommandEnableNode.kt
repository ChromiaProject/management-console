package net.postchain.mc.cli.node

import net.postchain.chain0.common.operations.enableNodeOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.pubkeyOption


class CommandEnableNode : DCBaseCommand(
        name = "enable",
        help = "Enables node"
) {
    private val key by pubkeyOption()

    override fun runDC() {
        transactionBuilder()
                .enableNodeOperation(clientProviderPubkey, key.data)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Node enabled",
                        "Cannot enable node"
                )
    }
}