package net.postchain.mc.cli.replica

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.operations.addBlockchainReplicaOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pubkeyOption

class CommandAddBlockchainReplica : CliktCommand(
        name = "add",
        help = "Add replica of a blockchain. The node is verifying but not building blocks."
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val nodePubKey by pubkeyOption().required()

    override fun run() {
        client.transactionBuilder()
                .addBlockchainReplicaOperation(
                        client.pubkey,
                        blockchainRID,
                        nodePubKey.data
                )
                .postAwaitConfirmation()
                .printResult(
                        "Replica added",
                        "Cannot add replica"
                )
    }

}