package net.postchain.mc.cli.node

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.operations.removeClusterNodeOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.pubkeyOption

class CommandRemoveNodeFromCluster : DCBaseCommand(
        requiresVersion = 109,
        name = "remove-from-cluster",
        help = "Removes node from a specific cluster"
) {
    private val key by pubkeyOption()

    private val cluster by option("-c", "--cluster", help = "Name of cluster to remove the node from")
            .required()

    override fun runDC() {
        transactionBuilder()
                .removeClusterNodeOperation(clientProviderPubkey, cluster, key.data)
                .postOrSave()
                .printResult(
                        "Node removed from cluster",
                        "Cannot remove node from cluster"
                )
    }
}