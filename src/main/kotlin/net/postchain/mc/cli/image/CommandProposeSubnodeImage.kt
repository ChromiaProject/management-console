package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.enum
import net.postchain.chain0.model.SubnodeImageType
import net.postchain.chain0.proposal_subnode_image.proposeSubnodeImageOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.baseComputeRequestsOptions
import net.postchain.mc.cli.util.digestValidator
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.metadataTextValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.scheduleAt
import net.postchain.mc.compatibility.ApiCompatV107.proposeSubnodeImageOperationV107
import net.postchain.mc.compatibility.ApiCompatV85.proposeSubnodeImageOperationV85
import net.postchain.mc.compatibility.ApiCompatV91.proposeSubnodeImageOperationV91

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
    private val scheduledTime by scheduleAt()
    private val baseComputeRequests by baseComputeRequestsOptions()
    private val nativeFunctions by option("-nf", "--native-funcs", help = "Rell native function implementations exposed by this subnode image (comma separated list of FQCNs)")
            .default("").validate(metadataTextValidator())
    private val description by proposalDescriptionOption { "Register new subnode image $name with URL $url and digest $digest" }

    override fun runDC() {
        if (scheduledTime != null && dcVersion < 86) {
            throw CliktError("--schedule-at is only supported in API version 86 or higher (current: $dcVersion)")
        }

        if (baseComputeRequests != null && dcVersion < 92) {
            throw CliktError("--base-compute-requests is only supported in API version 92 or higher (current: $dcVersion)")
        }

        transactionBuilder()
                .apply {
                    if (dcVersion < 86) {
                        proposeSubnodeImageOperationV85(clientProviderPubkey, name, url, digest, type, imageDescription, gtxModules, syncExts, description)
                    } else if (dcVersion < 92) {
                        proposeSubnodeImageOperationV91(clientProviderPubkey, name, url, digest, type, imageDescription,
                                gtxModules, syncExts, description, scheduledTime)
                    } else if (dcVersion < 108) {
                        proposeSubnodeImageOperationV107(clientProviderPubkey, name, url, digest, type, imageDescription,
                                gtxModules, syncExts, description, scheduledTime, baseComputeRequests?.toLong() ?: 0)
                    } else {
                        proposeSubnodeImageOperation(clientProviderPubkey, name, url, digest, type, imageDescription,
                                gtxModules, syncExts, description, scheduledTime,
                                baseComputeRequests = baseComputeRequests?.toLong() ?: 0, nativeFunctions)
                    }
                }
                .postOrSave()
                .printResult(
                        "Subnode image $name proposed",
                        "Cannot propose subnode image"
                )
    }
}
