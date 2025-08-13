package net.postchain.mc.cli.provider

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.common.operations.transferActionPointsOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.pubkeyOption


class CommandTransferActionPoints : DCBaseCommand(
        name = "transfer-action-points",
        help = "Transfer some of your action points to another provider"
) {
    private val pubkey by pubkeyOption()

    private val amount by option("-a", "--amount", help = "number of points to transfer").long().required()

    override fun runDC() {
        transactionBuilder()
                .transferActionPointsOperation(clientProviderPubkey, pubkey.data, amount)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Action points transferred",
                        "Transferring action points failed"
                )
    }
}