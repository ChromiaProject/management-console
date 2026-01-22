package net.postchain.mc.compatibility

import net.postchain.chain0.common.queries.GET_SUBNODE_IMAGE
import net.postchain.chain0.common.queries.GET_SUBNODE_IMAGES
import net.postchain.chain0.model.SubnodeImageType
import net.postchain.chain0.proposal_subnode_image.PROPOSE_SUBNODE_IMAGE
import net.postchain.client.core.PostchainQuery
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.types.WrappedByteArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.DefaultValue
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.toObject
import javax.annotation.processing.Generated

object ApiCompatV91 {

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
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_subnode_image:propose_subnode_image")
    fun TransactionBuilder.proposeSubnodeImageOperationV91(myPubkey: ByteArray,
                                                           name: String,
                                                           url: String,
                                                           digest: String,
                                                           subnodeImageType: SubnodeImageType,
                                                           imageDescription: String,
                                                           gtxModules: String,
                                                           syncExts: String,
                                                           proposalDescription: String,
                                                           scheduledAt: Long?) =
            addOperation(PROPOSE_SUBNODE_IMAGE, gtv(myPubkey),
                    gtv(name),
                    gtv(url),
                    gtv(digest),
                    gtv(subnodeImageType.ordinal.toLong()),
                    gtv(imageDescription),
                    gtv(gtxModules),
                    gtv(syncExts),
                    gtv(proposalDescription),
                    scheduledAt.let { if (it == null) GtvNull else gtv(it) })


    /**
     * Struct common.queries:subnode_image_data
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.queries:subnode_image_data")
    data class SubnodeImageDataV91(
            @param:Name("name") val name: String,
            @param:Name("url") val url: String,
            @param:Name("digest") val digest: String,
            @param:Name("subnode_image_type") val subnodeImageType: SubnodeImageType,
            @param:Name("owner") val owner: WrappedByteArray,
            @param:Name("active") val active: Boolean,
            @param:Name("description") val description: String,
            @param:Name("gtx_modules") val gtxModules: String,
            @param:Name("sync_exts") val syncExts: String,
            @param:Name("base_compute_requests") @param:DefaultValue(defaultLong = 0) val baseComputeRequests: Long,
    )

    /**
     * Query common.queries:get_subnode_image
     *
     * Returns subnode image data.
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.queries:get_subnode_image")
    fun PostchainQuery.getSubnodeImageV91(name: String) =
       query(GET_SUBNODE_IMAGE, gtv(mapOf("name" to gtv(name)))).toObject<SubnodeImageDataV91>()

    /**
     * Query common.queries:get_subnode_images
     *
     * Returns all subnode images.
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common.queries:get_subnode_images")
    fun PostchainQuery.getSubnodeImagesV91() =
       query(GET_SUBNODE_IMAGES, gtv(mapOf())).asArray().map { v1 -> v1.toObject<SubnodeImageDataV91>() }
}
