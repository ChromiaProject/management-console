package net.postchain.mc.cli.cluster.replica

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.operations.removeReplicaNodeFromClusterOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pubkeyOption

class CommandRemoveClusterReplica : CliktCommand(
        name = "remove",
        help = "Remove replica of a cluster"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOption("Cluster Name").required().validate(entityNameValidator())

    private val key by pubkeyOption()

    override fun run() {
        client.transactionBuilder()
                .removeReplicaNodeFromClusterOperation(
                        client.pubkey, key.data, name
                )
                .postAwaitConfirmation()
                .printResult(
                        "Cluster replica removed",
                        "Cannot remove cluster replica node"
                )
    }
}