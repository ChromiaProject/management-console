package net.postchain.mc.cli.jar_extension

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_subnode_jar_extension.proposeRemoveClusterSubnodeJarExtensionOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.scheduleAt

class CommandProposeRemoveSubnodeJarExtensionFromCluster : DCBaseCommand(
        name = "remove-from-cluster",
        help = "Remove subnode JAR extension from cluster",
        requiresVersion = 102,
) {
    private val clusterName by option("-cn", "--cluster-name", help = "Cluster name").required()
    private val subnodeJarExtensionName by option("-sje", "--subnode-jar-extension-name", help = "Subnode JAR extension name").required()
    private val scheduledTime by scheduleAt()

    private val description by proposalDescriptionOption { "Remove $subnodeJarExtensionName from $clusterName" }

    override fun runDC() {
        transactionBuilder()
                .apply {
                    proposeRemoveClusterSubnodeJarExtensionOperation(clientProviderPubkey, clusterName, subnodeJarExtensionName, description, scheduledTime)
                }
                .postOrSave()
                .printResult(
                        "Removing $subnodeJarExtensionName from $clusterName proposed",
                        "Cannot propose remove subnode JAR extension from cluster"
                )
    }
}
