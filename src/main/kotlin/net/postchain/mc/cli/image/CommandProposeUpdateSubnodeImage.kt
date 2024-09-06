package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.proposal_subnode_image.proposeUpdateSubnodeImageOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.digestValidator
import net.postchain.mc.cli.util.metadataTextValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion

class CommandProposeUpdateSubnodeImage : CliktCommand(
        name = "update",
        help = "Update subnode image"
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOption("Image name").required()
    private val url by option(help = "Image URL").required().validate(metadataTextValidator())
    private val digest by option(help = "Image digest").required().validate(digestValidator())

    private val description by proposalDescriptionOption { "Update subnode image $name with URL $url and digest $digest" }

    override fun run() {
        client.requireApiVersion(56)
        client.transactionBuilder()
                .proposeUpdateSubnodeImageOperation(client.pubkey, name, url, digest, description)
                .postAwaitConfirmation()
                .printResult(
                        "Subnode image $name update proposed",
                        "Cannot propose subnode image update",
                )
    }
}
