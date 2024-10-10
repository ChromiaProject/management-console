package net.postchain.mc.cli.image

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_subnode_image.proposeSubnodeImageStateOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion

class CommandProposeEnableSubnodeImage : PmcCommand(
        name = "enable",
        help = "Enable subnode image"
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOption("Image name").required()

    private val description by proposalDescriptionOption { "Enable subnode image $name" }

    override fun run() {
        client.requireApiVersion(56)
        client.transactionBuilder()
                .proposeSubnodeImageStateOperation(client.pubkey, name, true, description)
                .postAwaitConfirmation()
                .printResult(
                        "Subnode image $name enable proposed",
                        "Cannot propose subnode image enable"
                )
    }
}
