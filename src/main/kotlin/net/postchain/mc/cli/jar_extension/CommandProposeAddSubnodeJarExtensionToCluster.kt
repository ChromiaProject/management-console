package net.postchain.mc.cli.jar_extension

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_subnode_jar_extension.proposeAddClusterSubnodeJarExtensionOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.scheduleAt

class CommandProposeAddSubnodeJarExtensionToCluster : DCBaseCommand(
        name = "add-to-cluster",
        help = "Add subnode JAR extension to cluster",
        requiresVersion = 102
) {
    private val clusterName by option("-cn", "--cluster-name", help = "Cluster name").required()
    private val subnodeJarExtensionName by option("-sje", "--subnode-jar-extension-name", help = "Subnode JAR extension name").required()
    private val scheduledTime by scheduleAt()

    private val description by proposalDescriptionOption { "Add $subnodeJarExtensionName to $clusterName" }

    override fun runDC() {
        transactionBuilder()
                .apply {
                    proposeAddClusterSubnodeJarExtensionOperation(clientProviderPubkey, clusterName, subnodeJarExtensionName, description, scheduledTime)
                }
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Adding $subnodeJarExtensionName to $clusterName proposed",
                        "Cannot propose adding subnode JAR extension to cluster"
                )
    }
}
