package net.postchain.mc.cli.provider

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.common.operations.transferActionPointsOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pubkeyOption


class CommandTransferActionPoints : PmcCommand(
        name = "transfer-action-points",
        help = "Transfer some of your action points to another provider"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val pubkey by pubkeyOption()

    private val amount by option("-a", "--amount", help = "number of points to transfer").long().required()

    override fun run() {
        client.transactionBuilder()
                .transferActionPointsOperation(client.config.pubkey().data, pubkey.data, amount)
                .postAwaitConfirmation()
                .printResult(
                        "Action points transferred",
                        "Transferring action points failed"
                )
    }
}