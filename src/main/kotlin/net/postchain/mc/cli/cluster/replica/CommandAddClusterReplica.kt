package net.postchain.mc.cli.cluster.replica

import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.operations.addReplicaNodeToClusterOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pubkeyOption

class CommandAddClusterReplica : DCBaseCommand(
        name = "add",
        help = "add replica of a cluster"
) {
    private val name by nameOption("Cluster Name").required().validate(entityNameValidator())

    private val nodePubKey by pubkeyOption()

    override fun runDC() {
        transactionBuilder()
                .addReplicaNodeToClusterOperation(
                        clientProviderPubkey, nodePubKey.data, name
                )
                .postOrSave()
                .printResult(
                        "Cluster replica added",
                        "Cannot add cluster replica"
                )
    }

}