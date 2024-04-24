package net.postchain.mc.compatibility

import net.postchain.client.core.PostchainQuery
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.types.RowId
import net.postchain.common.types.WrappedByteArray
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.ToGtv
import net.postchain.gtv.mapper.toObject
import javax.annotation.processing.Generated

object ApiCompatECV21 {

    /*
    * Enum economy_chain.ec_proposal:ec_proposal_type
    */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain.ec_proposal:ec_proposal_type")
    enum class EcProposalTypeECV20: ToGtv {
        cluster_create,
        cluster_change_tag,
        tag_create,
        tag_update,
        tag_remove,
        economy_constants_update;

        override fun toGtv(): Gtv = gtv(ordinal.toLong())
    }

    /*
    * Enum economy_chain.ec_proposal:proposal_state
    */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain.ec_proposal:proposal_state")
    enum class ProposalStateECV20: ToGtv {
        PENDING,
        APPROVED,
        REJECTED,
        REVOKED;

        override fun toGtv(): Gtv = gtv(ordinal.toLong())
    }

    /*
    * Enum economy_chain.ec_proposal:voting_result
    */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain.ec_proposal:voting_result")
    enum class VotingResultECV20: ToGtv {
        pending,
        approved,
        rejected;

        override fun toGtv(): Gtv = gtv(ordinal.toLong())
    }

    /*
    * Struct economy_chain.ec_proposal:ec_proposal_voting_results
    */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain.ec_proposal:ec_proposal_voting_results")
    data class EcProposalVotingResultsECV20(
            @Name("positive_votes") val positiveVotes: Long,
            @Name("negative_votes") val negativeVotes: Long,
            @Name("max_votes") val maxVotes: Long,
            @Name("threshold") val threshold: Long,
            @Name("voting_result") val votingResult: VotingResultECV20
    )

    /*
    * Struct economy_chain.ec_proposal:ec_proposal_voter
    */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain.ec_proposal:ec_proposal_voter")
    data class EcProposalVoterECV20(
            @Name("provider") val provider: WrappedByteArray,
            @Name("vote") val vote: Boolean
    )

    /**
     * Query economy_chain.ec_proposal:get_proposals_range
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain.ec_proposal:get_proposals_range")
    fun PostchainQuery.getProposalsRangeECV20(from: Long,
                                         until: Long,
                                         onlyPending: Boolean) =
            query("get_proposals_range", gtv(mapOf("from" to gtv(from), "until" to gtv(until), "only_pending" to gtv(onlyPending)))).asArray().map { v1 -> v1.toObject<GetProposalsRangeResultECV20>() }

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetProposalsRangeResultECV20(
            @Name("rowid") val rowid: RowId,
            @Name("proposal_type") val proposalType: EcProposalTypeECV20,
            @Name("state") val state: ProposalStateECV20
    )

    /**
     * Query economy_chain.ec_proposal:get_relevant_proposals
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain.ec_proposal:get_relevant_proposals")
    fun PostchainQuery.getRelevantProposalsECV20(from: Long,
                                            until: Long,
                                            onlyPending: Boolean,
                                            myPubkey: ByteArray) =
            query("get_relevant_proposals", gtv(mapOf("from" to gtv(from), "until" to gtv(until), "only_pending" to gtv(onlyPending), "my_pubkey" to gtv(myPubkey)))).asArray().map { v1 -> v1.toObject<GetRelevantProposalsResult>() }

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetRelevantProposalsResult(
            @Name("rowid") val rowid: RowId,
            @Name("proposal_type") val proposalType: EcProposalTypeECV20,
            @Name("state") val state: ProposalStateECV20
    )

    /**
     * Query economy_chain.ec_proposal:get_proposal
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain.ec_proposal:get_proposal")
    fun PostchainQuery.getProposalECV20(id: RowId?) =
            query("get_proposal", gtv(mapOf("id" to id.let { if (it == null) GtvNull else gtv(it.id) }))).let { v0 -> if (v0 is GtvNull) null else v0.toObject<GetProposalResultECV20>() }

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetProposalResultECV20(
            @Name("id") val id: RowId,
            @Name("timestamp") val timestamp: Long,
            @Name("type") val type: EcProposalTypeECV20,
            @Name("proposed_by") val proposedBy: WrappedByteArray,
            @Name("description") val description: String,
            @Name("state") val state: ProposalStateECV20
    )

    /**
     * Query economy_chain.ec_proposal:get_proposal_voting_results
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain.ec_proposal:get_proposal_voting_results")
    fun PostchainQuery.getProposalVotingResultsECV20(rowid: RowId) =
            query("get_proposal_voting_results", gtv(mapOf("rowid" to gtv(rowid.id)))).toObject<EcProposalVotingResultsECV20>()

    /**
     * Query economy_chain.ec_proposal:get_proposal_voter_info
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain.ec_proposal:get_proposal_voter_info")
    fun PostchainQuery.getProposalVoterInfoECV20(rowid: RowId) =
            query("get_proposal_voter_info", gtv(mapOf("rowid" to gtv(rowid.id)))).asArray().map { v1 -> v1.toObject<EcProposalVoterECV20>() }

    /**
     * Query economy_chain.ec_proposal:get_provider_votes
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain.ec_proposal:get_provider_votes")
    fun PostchainQuery.getProviderVotesECV20(from: Long,
                                        until: Long,
                                        providerKey: ByteArray) =
            query("get_provider_votes", gtv(mapOf("from" to gtv(from), "until" to gtv(until), "provider_key" to gtv(providerKey)))).asArray().map { v1 -> v1.toObject<GetProviderVotesResultECV20>() }

    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "")
    data class GetProviderVotesResultECV20(
            @Name("proposal") val proposal: RowId,
            @Name("vote") val vote: Boolean
    )

    /**
     * Operation economy_chain.ec_proposal:make_vote
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain.ec_proposal:make_vote")
    fun TransactionBuilder.makeVoteOperationECV20(myPubkey: ByteArray,
                                             proposalId: RowId,
                                             vote: Boolean) =
            addOperation("make_vote", gtv(myPubkey),
                    gtv(proposalId.id),
                    gtv(vote))

    /**
     * Operation economy_chain.ec_proposal:revoke_proposal
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain.ec_proposal:revoke_proposal")
    fun TransactionBuilder.revokeProposalOperationECV20(myPubkey: ByteArray,
                                                   proposalId: RowId) =
            addOperation("revoke_proposal", gtv(myPubkey),
                    gtv(proposalId.id))

}