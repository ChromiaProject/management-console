package net.postchain.mc.compatibility

import net.postchain.chain0.model.SubnodeImageType
import net.postchain.chain0.proposal_subnode_image.GET_SUBNODE_IMAGE_PROPOSAL
import net.postchain.chain0.proposal_subnode_image.GET_UPDATE_SUBNODE_IMAGE_PROPOSAL
import net.postchain.chain0.proposal_subnode_image.PROPOSE_UPDATE_SUBNODE_IMAGE
import net.postchain.client.core.PostchainQuery
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.types.RowId
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.toObject
import javax.annotation.processing.Generated

object ApiCompatV95 {

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_subnode_image:propose_update_subnode_image")
    fun TransactionBuilder.proposeUpdateSubnodeImageOperationV95(myPubkey: ByteArray,
                                                              name: String,
                                                              url: String,
                                                              digest: String,
                                                              description: String,
                                                              scheduledAt: Long?) =
            addOperation(PROPOSE_UPDATE_SUBNODE_IMAGE, gtv(myPubkey),
                    gtv(name),
                    gtv(url),
                    gtv(digest),
                    gtv(description),
                    scheduledAt.let { if (it == null) GtvNull else gtv(it) })

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetSubnodeImageProposalResultV95(
            @param:Name("name") val name: String,
            @param:Name("url") val url: String,
            @param:Name("digest") val digest: String,
            @param:Name("subnode_image_type") val subnodeImageType: SubnodeImageType
    )

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_subnode_image:get_subnode_image_proposal")
    fun PostchainQuery.getSubnodeImageProposalV95(rowid: RowId) =
            query(GET_SUBNODE_IMAGE_PROPOSAL, gtv(mapOf("rowid" to gtv(rowid.id)))).let { v0 -> if (v0 is GtvNull) null else v0.toObject<GetSubnodeImageProposalResultV95>() }

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetUpdateSubnodeImageProposalResultV95(
            @param:Name("name") val name: String,
            @param:Name("url") val url: String,
            @param:Name("digest") val digest: String
    )

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal_subnode_image:get_update_subnode_image_proposal")
    fun PostchainQuery.getUpdateSubnodeImageProposalV95(rowid: RowId) =
            query(GET_UPDATE_SUBNODE_IMAGE_PROPOSAL, gtv(mapOf("rowid" to gtv(rowid.id)))).let { v0 -> if (v0 is GtvNull) null else v0.toObject<GetUpdateSubnodeImageProposalResultV95>() }

}
