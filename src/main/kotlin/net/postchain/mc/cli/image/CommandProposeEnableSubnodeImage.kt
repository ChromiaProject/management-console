package net.postchain.mc.cli.image

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_subnode_image.proposeSubnodeImageStateOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeEnableSubnodeImage : DCBaseCommand(
        name = "enable",
        help = "Enable subnode image",
        requiresVersion = 56,
) {
    private val name by nameOption("Image name").required()

    private val description by proposalDescriptionOption { "Enable subnode image $name" }

    override fun runDC() {
        client.transactionBuilder()
                .proposeSubnodeImageStateOperation(clientProviderPubkey, name, true, description)
                .postAwaitConfirmation()
                .printResult(
                        "Subnode image $name enable proposed",
                        "Cannot propose subnode image enable"
                )
    }
}
