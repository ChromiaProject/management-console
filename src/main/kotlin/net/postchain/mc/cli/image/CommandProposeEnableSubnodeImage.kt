package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_subnode_image.proposeSubnodeImageStateOperation
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.scheduleAt
import net.postchain.mc.compatibility.ApiCompatV85.proposeSubnodeImageStateOperationV85

class CommandProposeEnableSubnodeImage : DCBaseCommand(
        name = "enable",
        help = "Enable subnode image",
        requiresVersion = 56,
) {
    private val name by nameOption("Image name").required()
    private val scheduledTime by scheduleAt()

    private val description by proposalDescriptionOption { "Enable subnode image $name" }

    override fun runDC() {
        val apiVersion = client.apiVersion()
        
        if (scheduledTime != null && apiVersion < 86) {
            throw CliktError("--schedule-at is only supported in API version 86 or higher (current: $apiVersion)")
        }
        
        client.transactionBuilder()
                .apply {
                    if (apiVersion < 86) {
                        proposeSubnodeImageStateOperationV85(clientProviderPubkey, name, true, description)
                    } else {
                        proposeSubnodeImageStateOperation(clientProviderPubkey, name, true, description, scheduledTime)
                    }
                }
                .postAwaitConfirmation()
                .printResult(
                        "Subnode image $name enable proposed",
                        "Cannot propose subnode image enable"
                )
    }
}
