package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.enum
import net.postchain.chain0.model.SubnodeImageType
import net.postchain.chain0.proposal_subnode_image.proposeSubnodeImageOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.metadataTextValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion

class CommandProposeSubnodeImage : CliktCommand(
        name = "add",
        help = "Register new subnode image"
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOption("Image name").required()
    private val url by option(help = "Image URL").required()
    private val digest by option(help = "Image digest").required()
    private val type by option(help = "Image type").enum<SubnodeImageType>().default(SubnodeImageType.COMMON)
    private val imageDescription by option("-desc", "--image-description", help = "Subnode image description").required()
            .validate(metadataTextValidator())
    private val gtxModules by option("-gtx", "--gtx-modules", help = "GTX modules exposed by this subnode image (comma separated list of FQCNs)")
            .default("").validate(metadataTextValidator())
    private val syncExts by option("-sync", "--sync-exts", help = "Synchronization infrastructure extensions exposed by this subnode image (comma separated list of FQCNs)")
            .default("").validate(metadataTextValidator())

    private val description by proposalDescriptionOption { "Register new subnode image $name with URL $url and digest $digest" }

    override fun run() {
        client.requireApiVersion(59)
        client.transactionBuilder()
                .proposeSubnodeImageOperation(client.pubkey, name, url, digest, type, imageDescription, description, gtxModules, syncExts)
                .postAwaitConfirmation()
                .printResult(
                        "Subnode image $name proposed",
                        "Cannot propose subnode image"
                )
    }
}
