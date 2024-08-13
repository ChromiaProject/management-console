package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_subnode_image.proposeAddClusterSubnodeImageOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion

class CommandProposeAddSubnodeImageToCluster : CliktCommand(
        name = "add-to-cluster",
        help = "Add subnode image to cluster"
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val clusterName by option("-cn", "--cluster-name", help = "Cluster name").required()
    private val subnodeImageName by option("-sin", "--subnode-image-name", help = "Subnode image name").required()

    private val description by proposalDescriptionOption { "Add $subnodeImageName to $clusterName" }

    override fun run() {
        client.requireApiVersion(56)
        client.transactionBuilder()
                .proposeAddClusterSubnodeImageOperation(client.pubkey, clusterName, subnodeImageName, description)
                .postAwaitConfirmation()
                .printResult(
                        "Adding $subnodeImageName to $clusterName proposed",
                        "Cannot propose adding subnode image to cluster"
                )
    }
}
