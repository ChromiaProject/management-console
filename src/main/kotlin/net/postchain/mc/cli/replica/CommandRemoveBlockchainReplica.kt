package net.postchain.mc.cli.replica

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.operations.removeBlockchainReplicaOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pubkeyOption

class CommandRemoveBlockchainReplica : CliktCommand(
        name = "remove",
        help = "Remove replica of a blockchain"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val key by pubkeyOption()

    override fun run() {
        client.transactionBuilder()
                .removeBlockchainReplicaOperation(
                        client.pubkey,
                        blockchainRID,
                        key.data
                )
                .postAwaitConfirmation()
                .printResult(
                        "Replica removed",
                        "Cannot remove replica node"
                )
    }
}