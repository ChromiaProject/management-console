package net.postchain.mc.cli.cluster

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_cluster.proposeRemoveClusterOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption


class CommandProposeRemoveCluster : DCBaseCommand(
        name = "remove",
        help = "Propose removal of cluster. Command is irreversible"
) {
    private val name by nameOption("Cluster name to remove").required()

    private val description by proposalDescriptionOption { "Remove cluster $name" }

    override fun runDC() {
        client.transactionBuilder()
                .proposeRemoveClusterOperation(clientProviderPubkey, name, description)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Cluster removal proposed",
                        "Failed proposing cluster removal"
                )
    }
}