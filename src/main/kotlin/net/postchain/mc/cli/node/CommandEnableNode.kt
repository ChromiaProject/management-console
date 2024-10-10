package net.postchain.mc.cli.node

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.operations.enableNodeOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pubkeyOption


class CommandEnableNode : PmcCommand(
        name = "enable",
        help = "Enables node"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val key by pubkeyOption()

    override fun run() {
        client.transactionBuilder()
                .enableNodeOperation(client.config.pubkey().data, key.data)
                .postAwaitConfirmation()
                .printResult(
                        "Node enabled",
                        "Cannot enable node"
                )
    }
}