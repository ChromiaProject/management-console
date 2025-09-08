package net.postchain.mc.cli.replica

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.operations.removeBlockchainReplicaOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pubkeyOption

class CommandRemoveBlockchainReplica : DCBaseCommand(
        name = "remove",
        help = "Remove replica of a blockchain"
) {
    private val blockchainRID by blockchainRidOption().required()

    private val key by pubkeyOption()

    override fun runDC() {
        transactionBuilder()
                .removeBlockchainReplicaOperation(
                        clientProviderPubkey,
                        blockchainRID,
                        key.data
                )
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Replica removed",
                        "Cannot remove replica node"
                )
    }
}
