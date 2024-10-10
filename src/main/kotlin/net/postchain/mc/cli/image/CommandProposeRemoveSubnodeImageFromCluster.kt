package net.postchain.mc.cli.image

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_subnode_image.proposeRemoveClusterSubnodeImageOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion

class CommandProposeRemoveSubnodeImageFromCluster : PmcCommand(
        name = "remove-from-cluster",
        help = "Remove subnode image from cluster"
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val clusterName by option("-cn", "--cluster-name", help = "Cluster name").required()
    private val subnodeImageName by option("-sin", "--subnode-image-name", help = "Subnode image name").required()

    private val description by proposalDescriptionOption { "Remove $subnodeImageName from $clusterName" }

    override fun run() {
        client.requireApiVersion(56)
        client.transactionBuilder()
                .proposeRemoveClusterSubnodeImageOperation(client.pubkey, clusterName, subnodeImageName, description)
                .postAwaitConfirmation()
                .printResult(
                        "Removing $subnodeImageName from $clusterName proposed",
                        "Cannot propose remove subnode image from cluster"
                )
    }
}
