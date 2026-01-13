package net.postchain.mc.compatibility

import net.postchain.chain0.model.SubnodeImageType
import net.postchain.chain0.model.SubnodeJarExtensionType
import net.postchain.chain0.proposal_subnode_image.PROPOSE_SUBNODE_IMAGE
import net.postchain.chain0.proposal_subnode_jar_extension.PROPOSE_SUBNODE_JAR_EXTENSION
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import javax.annotation.processing.Generated

object ApiCompatV107 {

    /**
     * Operation proposal_subnode_image:propose_subnode_image
     *
     * Propose new subnode image.
     *
     * Permission: system provider
     *
     * Rate limit: actions
     * @param myPubkey pubkey of provider
     * @param gtxModules GTX modules exposed by this subnode image (comma separated list of FQCNs)
     * @param syncExts Synchronization infrastructure extensions exposed by this subnode image (comma separated list of FQCNs)
     * @param scheduledAt The time to apply this proposal when approved. If the proposal is approved after the time has passed the proposal is applied right away.
     * @param baseComputeRequests How many compute requests per week a container gets by default.
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_subnode_image:propose_subnode_image")
    fun TransactionBuilder.proposeSubnodeImageOperationV107(myPubkey: ByteArray,
                                                            name: String,
                                                            url: String,
                                                            digest: String,
                                                            subnodeImageType: SubnodeImageType,
                                                            imageDescription: String,
                                                            gtxModules: String,
                                                            syncExts: String,
                                                            proposalDescription: String,
                                                            scheduledAt: Long?,
                                                            baseComputeRequests: Long) =
            addOperation(PROPOSE_SUBNODE_IMAGE, gtv(myPubkey),
                    gtv(name),
                    gtv(url),
                    gtv(digest),
                    gtv(subnodeImageType.ordinal.toLong()),
                    gtv(imageDescription),
                    gtv(gtxModules),
                    gtv(syncExts),
                    gtv(proposalDescription),
                    scheduledAt.let { if (it == null) GtvNull else gtv(it) },
                    gtv(baseComputeRequests))

    /**
     * Operation proposal_subnode_jar_extension:propose_subnode_jar_extension
     *
     * Propose new Subnode JAR extension.
     *
     * Permission: system provider
     *
     * Rate limit: actions
     * @param myPubkey Pubkey of the provider creating this proposal
     * @param jar Raw JAR file content for the extension in bytes
     * @param subnodeJarExtensionType Type of extension, common to or specific to certain clusters
     * @param extensionDescription Description of the new extension
     * @param gtxModules GTX modules exposed by this Subnode JAR extension (comma separated list of FQCNs)
     * @param syncExts Synchronization infrastructure extensions exposed by this Subnode JAR extension (comma separated list of FQCNs)
     * @param proposalDescription Description of the proposal
     * @param scheduledAt The time to apply this proposal when approved. If the proposal is approved after the time has passed the proposal is applied right away.
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_subnode_jar_extension:propose_subnode_jar_extension")
    fun TransactionBuilder.proposeSubnodeJarExtensionOperationV107(myPubkey: ByteArray,
                                                                   name: String,
                                                                   jar: ByteArray,
                                                                   subnodeJarExtensionType: SubnodeJarExtensionType,
                                                                   extensionDescription: String,
                                                                   gtxModules: String,
                                                                   syncExts: String,
                                                                   proposalDescription: String,
                                                                   scheduledAt: Long?) =
            addOperation(PROPOSE_SUBNODE_JAR_EXTENSION, gtv(myPubkey),
                    gtv(name),
                    gtv(jar),
                    gtv(subnodeJarExtensionType.ordinal.toLong()),
                    gtv(extensionDescription),
                    gtv(gtxModules),
                    gtv(syncExts),
                    gtv(proposalDescription),
                    scheduledAt.let { if (it == null) GtvNull else gtv(it) })
}
