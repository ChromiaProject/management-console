package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_subnode_image.proposeAddClusterSubnodeImageOperation
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.scheduleAt
import net.postchain.mc.compatibility.ApiCompatV85.proposeAddClusterSubnodeImageOperationV85

class CommandProposeAddSubnodeImageToCluster : DCBaseCommand(
        name = "add-to-cluster",
        help = "Add subnode image to cluster",
        requiresVersion = 56
) {
    private val clusterName by option("-cn", "--cluster-name", help = "Cluster name").required()
    private val subnodeImageName by option("-sin", "--subnode-image-name", help = "Subnode image name").required()
    private val scheduledTime by scheduleAt()

    private val description by proposalDescriptionOption { "Add $subnodeImageName to $clusterName" }

    override fun runDC() {
        val apiVersion = client.apiVersion()
        
        if (scheduledTime != null && apiVersion < 86) {
            throw CliktError("--schedule-at is only supported in API version 86 or higher (current: $apiVersion)")
        }
        
        client.transactionBuilder()
                .apply {
                    if (apiVersion < 86) {
                        proposeAddClusterSubnodeImageOperationV85(clientProviderPubkey, clusterName, subnodeImageName, description)
                    } else {
                        proposeAddClusterSubnodeImageOperation(clientProviderPubkey, clusterName, subnodeImageName, description, scheduledTime)
                    }
                }
                .postAwaitConfirmation()
                .printResult(
                        "Adding $subnodeImageName to $clusterName proposed",
                        "Cannot propose adding subnode image to cluster"
                )
    }
}
