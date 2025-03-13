package net.postchain.mc.cli.image

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_subnode_image.proposeAddClusterSubnodeImageOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeAddSubnodeImageToCluster : DCBaseCommand(
        name = "add-to-cluster",
        help = "Add subnode image to cluster",
        requiresVersion = 56
) {
    private val clusterName by option("-cn", "--cluster-name", help = "Cluster name").required()
    private val subnodeImageName by option("-sin", "--subnode-image-name", help = "Subnode image name").required()

    private val description by proposalDescriptionOption { "Add $subnodeImageName to $clusterName" }

    override fun runDC() {
        client.transactionBuilder()
                .proposeAddClusterSubnodeImageOperation(clientProviderPubkey, clusterName, subnodeImageName, description)
                .postAwaitConfirmation()
                .printResult(
                        "Adding $subnodeImageName to $clusterName proposed",
                        "Cannot propose adding subnode image to cluster"
                )
    }
}
