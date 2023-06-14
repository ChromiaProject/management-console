package net.postchain.mc.compatibility

import net.postchain.chain0.proposal.ProposalType
import net.postchain.client.core.PostchainQuery
import net.postchain.common.types.RowId
import net.postchain.common.types.WrappedByteArray
import net.postchain.gtv.GtvFactory
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.toObject

object ApiCompatV6 {

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal:get_proposals_since")
    fun PostchainQuery.getProposalsSinceV6(since: RowId) =
            query("get_proposals_since", GtvFactory.gtv(mapOf("since" to GtvFactory.gtv(since.id)))).asArray().map { v -> v.toObject<GetProposalsSinceResultV6>() }

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetProposalsSinceResultV6(
            @Name("rowid") val rowid: RowId,
            @Name("proposal_type") val proposalType: ProposalType
    )

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal:get_relevant_proposals")
    fun PostchainQuery.getRelevantProposalsV6(myPubkey: ByteArray,
                                              since: RowId) =
            query("get_relevant_proposals", GtvFactory.gtv(mapOf("my_pubkey" to GtvFactory.gtv(myPubkey), "since" to GtvFactory.gtv(since.id)))).asArray().map { v -> v.toObject<GetRelevantProposalsResultV6>() }

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetRelevantProposalsResultV6(
            @Name("rowid") val rowid: RowId,
            @Name("proposal_type") val proposalType: ProposalType
    )

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal:get_proposal")
    fun PostchainQuery.getProposalV6(id: RowId?) =
            query("get_proposal", GtvFactory.gtv(mapOf("id" to id.let { if (it == null) GtvNull else GtvFactory.gtv(it.id) }))).let { v -> if (v is GtvNull) null else v.toObject<GetProposalResultV6>() }

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetProposalResultV6(
            @Name("id") val id: RowId,
            @Name("timestamp") val timestamp: Long,
            @Name("type") val type: ProposalType,
            @Name("proposed_by") val proposedBy: WrappedByteArray,
            @Name("description") val description: String
    )

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "proposal.voting:get_provider_votes")
    fun PostchainQuery.getProviderVotesV6(providerKey: ByteArray) =
            query("get_provider_votes", GtvFactory.gtv(mapOf("provider_key" to GtvFactory.gtv(providerKey)))).asArray().map { v -> v.toObject<GetProviderVotesResultV6>() }

    // @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetProviderVotesResultV6(
            @Name("proposal") val proposal: RowId,
            @Name("vote") val vote: Boolean
    )
}