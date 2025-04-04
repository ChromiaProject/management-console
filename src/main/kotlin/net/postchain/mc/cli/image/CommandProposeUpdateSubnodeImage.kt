package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.queries.getSubnodeImage
import net.postchain.chain0.proposal_subnode_image.proposeUpdateSubnodeImageOperation
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.digestValidator
import net.postchain.mc.cli.util.metadataTextValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.scheduleAt
import net.postchain.mc.compatibility.ApiCompatV85.proposeUpdateSubnodeImageOperationV85

class CommandProposeUpdateSubnodeImage : DCBaseCommand(
        name = "update",
        help = "Update subnode image",
        requiresVersion = 56,
) {
    private val name by nameOption("Image name").required()
    private val url by option(help = "Image URL")
            .defaultLazy {
                client.getSubnodeImage(name).url
            }
            .validate(metadataTextValidator())
    private val digest by option(help = "Image digest").required().validate(digestValidator())
    private val scheduledTime by scheduleAt()

    private val description by proposalDescriptionOption { "Update subnode image $name with URL $url and digest $digest" }

    override fun runDC() {
        val apiVersion = client.apiVersion()
        
        if (scheduledTime != null && apiVersion < 86) {
            throw CliktError("--schedule-at is only supported in API version 86 or higher (current: $apiVersion)")
        }
        
        client.transactionBuilder()
                .apply {
                    if (apiVersion < 86) {
                        proposeUpdateSubnodeImageOperationV85(clientProviderPubkey, name, url, digest, description)
                    } else {
                        proposeUpdateSubnodeImageOperation(clientProviderPubkey, name, url, digest, description, scheduledTime)
                    }
                }
                .postAwaitConfirmation()
                .printResult(
                        "Subnode image $name update proposed",
                        "Cannot propose subnode image update",
                )
    }
}
