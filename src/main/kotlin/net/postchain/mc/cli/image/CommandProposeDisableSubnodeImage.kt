package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_subnode_image.proposeSubnodeImageStateOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.scheduleAt
import net.postchain.mc.compatibility.ApiCompatV85.proposeSubnodeImageStateOperationV85

class CommandProposeDisableSubnodeImage : DCBaseCommand(
        name = "disable",
        help = "Disable subnode image",
        requiresVersion = 56,
) {
    private val name by nameOption("Image name").required()
    private val scheduledTime by scheduleAt()

    private val description by proposalDescriptionOption { "Disable subnode image $name" }

    override fun runDC() {
        if (scheduledTime != null && dcVersion < 86) {
            throw CliktError("--schedule-at is only supported in API version 86 or higher (current: $dcVersion)")
        }
        
        client.transactionBuilder()
                .apply {
                    if (dcVersion < 86) {
                        proposeSubnodeImageStateOperationV85(clientProviderPubkey, name, false, description)
                    } else {
                        proposeSubnodeImageStateOperation(clientProviderPubkey, name, false, description, scheduledTime)
                    }
                }
                .postAwaitConfirmation()
                .printResult(
                        "Subnode image $name disable proposed",
                        "Cannot propose subnode image disable"
                )
    }
}
