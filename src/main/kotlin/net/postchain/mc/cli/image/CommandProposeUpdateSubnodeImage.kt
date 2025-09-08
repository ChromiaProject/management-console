package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.queries.getSubnodeImage
import net.postchain.chain0.proposal_subnode_image.proposeUpdateSubnodeImageOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.baseComputeRequestsOptions
import net.postchain.mc.cli.util.digestValidator
import net.postchain.mc.cli.util.metadataTextValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.scheduleAt
import net.postchain.mc.compatibility.ApiCompatV85.proposeUpdateSubnodeImageOperationV85
import net.postchain.mc.compatibility.ApiCompatV95.proposeUpdateSubnodeImageOperationV95

class CommandProposeUpdateSubnodeImage : DCBaseCommand(
        name = "update",
        help = "Update subnode image",
        requiresVersion = 56,
) {
    private val name by nameOption("Image name").required()
    private val url by option(help = "Image URL").validate(metadataTextValidator())
    private val digest by option(help = "Image digest").validate(digestValidator())
    private val imageDescription by option("--image-description", help = "Subnode image description")
            .validate(metadataTextValidator())
    private val gtxModules by option("-gtx", "--gtx-modules", help = "GTX modules exposed by this subnode image (comma separated list of FQCNs)")
            .validate(metadataTextValidator())
    private val syncExts by option("-sync", "--sync-exts", help = "Synchronization infrastructure extensions exposed by this subnode image (comma separated list of FQCNs)")
            .validate(metadataTextValidator())
    private val scheduledTime by scheduleAt()
    private val baseComputeRequests by baseComputeRequestsOptions()
    private val proposalDescription by proposalDescriptionOption { "Update subnode image $name" }

    private val urlV95: String by lazy {
        url ?: run {
            client.getSubnodeImage(name).url
        }
    }

    override fun runDC() {
        transactionBuilder()
                .apply {
                    if (dcVersion < 96) {
                        if (digest == null) {
                            throw CliktError("Error: missing option --digest (required for API version < 96)")
                        }

                        if (dcVersion < 86) {

                            if (scheduledTime != null) {
                                throw CliktError("--schedule-at is only supported in API version 86 or higher (current: $dcVersion)")
                            }

                            proposeUpdateSubnodeImageOperationV85(clientProviderPubkey,
                                    name, urlV95, digest!!, proposalDescription)
                        } else { // 86 < version < 96
                            proposeUpdateSubnodeImageOperationV95(clientProviderPubkey,
                                    name, urlV95, digest!!, proposalDescription, scheduledTime)
                        }
                    } else {

                        listOf(
                                url,
                                digest,
                                imageDescription,
                                gtxModules,
                                syncExts,
                                baseComputeRequests
                        ).let {
                            if (it.all { o -> o == null }) {
                                throw CliktError("Error: at least one image data option must be provided (--url, --digest, --image-description, --gtx-modules, --sync-exts, --base-compute-requests)")
                            }
                        }

                        proposeUpdateSubnodeImageOperation(clientProviderPubkey,
                                name, url, digest,
                                imageDescription,
                                gtxModules,
                                syncExts,
                                proposalDescription,
                                scheduledTime,
                                baseComputeRequests?.toLong()
                        )
                    }
                }
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Subnode image $name update proposed",
                        "Cannot propose subnode image update",
                )
    }
}
