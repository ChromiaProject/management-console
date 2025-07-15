package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_subnode_image.proposeRemoveClusterSubnodeImageOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.scheduleAt
import net.postchain.mc.compatibility.ApiCompatV85.proposeRemoveClusterSubnodeImageOperationV85

class CommandProposeRemoveSubnodeImageFromCluster : DCBaseCommand(
        name = "remove-from-cluster",
        help = "Remove subnode image from cluster",
        requiresVersion = 56,
) {
    private val clusterName by option("-cn", "--cluster-name", help = "Cluster name").required()
    private val subnodeImageName by option("-sin", "--subnode-image-name", help = "Subnode image name").required()
    private val scheduledTime by scheduleAt()

    private val description by proposalDescriptionOption { "Remove $subnodeImageName from $clusterName" }

    override fun runDC() {
        if (scheduledTime != null && dcVersion < 86) {
            throw CliktError("--schedule-at is only supported in API version 86 or higher (current: $dcVersion)")
        }
        
        client.transactionBuilder()
                .apply {
                    if (dcVersion < 86) {
                        proposeRemoveClusterSubnodeImageOperationV85(clientProviderPubkey, clusterName, subnodeImageName, description)
                    } else {
                        proposeRemoveClusterSubnodeImageOperation(clientProviderPubkey, clusterName, subnodeImageName, description, scheduledTime)
                    }
                }
                .postAwaitConfirmation()
                .printResult(
                        "Removing $subnodeImageName from $clusterName proposed",
                        "Cannot propose remove subnode image from cluster"
                )
    }
}
