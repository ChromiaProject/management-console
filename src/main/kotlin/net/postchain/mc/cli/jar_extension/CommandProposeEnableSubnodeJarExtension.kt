package net.postchain.mc.cli.jar_extension

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_subnode_jar_extension.proposeSubnodeJarExtensionStateOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.scheduleAt

class CommandProposeEnableSubnodeJarExtension : DCBaseCommand(
        name = "enable",
        help = "Enable subnode JAR extension",
        requiresVersion = 102,
) {
    private val name by nameOption("JAR extension name").required()
    private val scheduledTime by scheduleAt()

    private val description by proposalDescriptionOption { "Enable subnode JAR extension $name" }

    override fun runDC() {
        transactionBuilder()
                .apply {
                    proposeSubnodeJarExtensionStateOperation(clientProviderPubkey, name, true, description, scheduledTime)
                }
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Subnode JAR extension $name enable proposed",
                        "Cannot propose subnode JAR extension enable"
                )
    }
}
