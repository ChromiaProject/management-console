package net.postchain.mc.cli.image

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.enum
import net.postchain.chain0.model.SubnodeImageType
import net.postchain.chain0.proposal_subnode_image.proposeSubnodeImageOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.digestValidator
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.metadataTextValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeSubnodeImage : DCBaseCommand(
        name = "add",
        help = "Register new subnode image",
        requiresVersion = 59,
) {
    private val name by nameOption("Image name").required().validate(entityNameValidator())
    private val url by option(help = "Image URL").required().validate(metadataTextValidator())
    private val digest by option(help = "Image digest").required().validate(digestValidator())
    private val type by option(help = "Image type").enum<SubnodeImageType>().default(SubnodeImageType.COMMON)
    private val imageDescription by option("-desc", "--image-description", help = "Subnode image description").required()
            .validate(metadataTextValidator())
    private val gtxModules by option("-gtx", "--gtx-modules", help = "GTX modules exposed by this subnode image (comma separated list of FQCNs)")
            .default("").validate(metadataTextValidator())
    private val syncExts by option("-sync", "--sync-exts", help = "Synchronization infrastructure extensions exposed by this subnode image (comma separated list of FQCNs)")
            .default("").validate(metadataTextValidator())

    private val description by proposalDescriptionOption { "Register new subnode image $name with URL $url and digest $digest" }

    override fun runDC() {
        client.transactionBuilder()
                .proposeSubnodeImageOperation(clientProviderPubkey, name, url, digest, type, imageDescription, gtxModules, syncExts, description)
                .postAwaitConfirmation()
                .printResult(
                        "Subnode image $name proposed",
                        "Cannot propose subnode image"
                )
    }
}
