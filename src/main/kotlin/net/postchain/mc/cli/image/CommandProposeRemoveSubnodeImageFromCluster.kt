package net.postchain.mc.cli.image

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_subnode_image.proposeRemoveClusterSubnodeImageOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeRemoveSubnodeImageFromCluster : DCBaseCommand(
        name = "remove-from-cluster",
        help = "Remove subnode image from cluster",
        requiresVersion = 56,
) {
    private val clusterName by option("-cn", "--cluster-name", help = "Cluster name").required()
    private val subnodeImageName by option("-sin", "--subnode-image-name", help = "Subnode image name").required()

    private val description by proposalDescriptionOption { "Remove $subnodeImageName from $clusterName" }

    override fun runDC() {
        client.transactionBuilder()
                .proposeRemoveClusterSubnodeImageOperation(clientProviderPubkey, clusterName, subnodeImageName, description)
                .postAwaitConfirmation()
                .printResult(
                        "Removing $subnodeImageName from $clusterName proposed",
                        "Cannot propose remove subnode image from cluster"
                )
    }
}
