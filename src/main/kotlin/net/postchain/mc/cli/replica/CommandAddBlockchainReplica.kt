package net.postchain.mc.cli.replica

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.operations.addBlockchainReplicaOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pubkeyOption

class CommandAddBlockchainReplica : DCBaseCommand(
        name = "add",
        help = "Add replica of a blockchain. The node is verifying but not building blocks."
) {
    private val blockchainRID by blockchainRidOption().required()

    private val nodePubKey by pubkeyOption()

    override fun runDC() {
        client.transactionBuilder()
                .addBlockchainReplicaOperation(
                        clientProviderPubkey,
                        blockchainRID,
                        nodePubKey.data
                )
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Replica added",
                        "Cannot add replica"
                )
    }

}