package net.postchain.mc.cli.cluster.replica

import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.operations.removeReplicaNodeFromClusterOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pubkeyOption

class CommandRemoveClusterReplica : DCBaseCommand(
        name = "remove",
        help = "Remove replica of a cluster"
) {
    private val name by nameOption("Cluster Name").required().validate(entityNameValidator())

    private val key by pubkeyOption()

    override fun runDC() {
        client.transactionBuilder()
                .removeReplicaNodeFromClusterOperation(
                        clientProviderPubkey, key.data, name
                )
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Cluster replica removed",
                        "Cannot remove cluster replica node"
                )
    }
}