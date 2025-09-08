package net.postchain.mc.compatibility

import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.types.RowId
import net.postchain.crypto.PubKey
import net.postchain.economy.economy_chain.PendingPriceOracleRateData
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.GtvObjectMapper
import java.math.BigDecimal
import javax.annotation.processing.Generated

object ApiCompatECV57 {

    const val CREATE_CLUSTER = "create_cluster"
    const val CREATE_TAG = "create_tag"
    const val CHANGE_CLUSTER_TAG = "change_cluster_tag"
    const val FORCE_REMOVE_CONTAINER = "force_remove_container"
    const val REMOVE_TAG = "remove_tag"
    const val UPDATE_ECONOMY_CONSTANTS = "update_economy_constants"
    const val PROPOSE_PRICE_ORACLE_RATE = "propose_price_oracle_rate"
    const val PROPOSE_STAKING_REQUIREMENT_CONSTANTS = "propose_staking_requirement_constants"
    const val UPDATE_TAG = "update_tag"
    const val REVOKE_COMMON_PROPOSAL_V65 = "revoke_common_proposal_v65"
    const val MAKE_COMMON_VOTE_V65 = "make_common_vote_v65"
    const val PROPOSE_SYSTEM_PROVIDER_ECONOMY_CONSTANTS = "propose_system_provider_economy_constants"
    const val REVOKE_COMMON_PROPOSAL = "revoke_common_proposal"
    const val MAKE_COMMON_VOTE = "make_common_vote"

    /**
     * Operation common_proposal:make_common_vote
     *
     * Make common vote.
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common_proposal:make_common_vote")
    fun TransactionBuilder.makeCommonVoteOperation(pubkey: PubKey,
                                                   proposalId: RowId,
                                                   vote: Boolean) =
            addOperation(MAKE_COMMON_VOTE, gtv(pubkey.data),
                    gtv(proposalId.id),
                    gtv(vote))
    /**
     * Operation common_proposal:revoke_common_proposal
     *
     * Revoke common proposal.
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common_proposal:revoke_common_proposal")
    fun TransactionBuilder.revokeCommonProposalOperation(pubkey: PubKey,
                                                         proposalId: RowId) =
            addOperation(REVOKE_COMMON_PROPOSAL, gtv(pubkey.data),
                    gtv(proposalId.id))

    /**
     * Operation economy_chain:create_cluster
     *
     *
     * @param extraStorage MiB
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:create_cluster")
    fun TransactionBuilder.createClusterOperation(name: String,
                                                  governorVoterSetName: String,
                                                  voterSetName: String,
                                                  clusterUnits: Long,
                                                  extraStorage: Long,
                                                  tagName: String) =
            addOperation(CREATE_CLUSTER, gtv(name),
                    gtv(governorVoterSetName),
                    gtv(voterSetName),
                    gtv(clusterUnits),
                    gtv(extraStorage),
                    gtv(tagName))

    /**
     * Operation economy_chain:create_tag
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:create_tag")
    fun TransactionBuilder.createTagOperationV57(name: String,
                                                 scuPrice: Long,
                                                 extraStoragePrice: Long) =
            addOperation(CREATE_TAG, gtv(name),
                    gtv(scuPrice),
                    gtv(extraStoragePrice))

    /**
     * Operation economy_chain:change_cluster_tag
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:change_cluster_tag")
    fun TransactionBuilder.changeClusterTagOperation(clusterName: String,
                                                     tagName: String) =
            addOperation(CHANGE_CLUSTER_TAG, gtv(clusterName),
                    gtv(tagName))

    /**
     * Operation economy_chain_remove_container:force_remove_container
     *
     * Force removal of a container and its lease without refund.
     *
     * An ICMF message will be sent to DC for deleting the container.
     *
     * Permission: system provider
     * @param containerName container name
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain_remove_container:force_remove_container")
    fun TransactionBuilder.forceRemoveContainerOperation(containerName: String) =
            addOperation(FORCE_REMOVE_CONTAINER, gtv(containerName))

    /**
     * Operation economy_chain:remove_tag
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:remove_tag")
    fun TransactionBuilder.removeTagOperation(name: String) =
            addOperation(REMOVE_TAG, gtv(name))

    /**
     * Operation economy_chain:update_economy_constants
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:update_economy_constants")
    fun TransactionBuilder.updateEconomyConstantsOperation(minLeaseTimeWeeks: Long?,
                                                           maxLeaseTimeWeeks: Long?,
                                                           stakingRewardRate: BigDecimal?,
                                                           stakingRewardFeeShare: BigDecimal?,
                                                           chromiaFoundationFeeShare: BigDecimal?,
                                                           resourcePoolMarginFeeShare: BigDecimal?,
                                                           dappProviderRiskShare: BigDecimal?,
                                                           scheduledAt: Long?) =
            addOperation(UPDATE_ECONOMY_CONSTANTS, minLeaseTimeWeeks.let { if (it == null) GtvNull else gtv(it) },
                    maxLeaseTimeWeeks.let { if (it == null) GtvNull else gtv(it) },
                    stakingRewardRate.let { if (it == null) GtvNull else gtv(it.toString()) },
                    stakingRewardFeeShare.let { if (it == null) GtvNull else gtv(it.toString()) },
                    chromiaFoundationFeeShare.let { if (it == null) GtvNull else gtv(it.toString()) },
                    resourcePoolMarginFeeShare.let { if (it == null) GtvNull else gtv(it.toString()) },
                    dappProviderRiskShare.let { if (it == null) GtvNull else gtv(it.toString()) },
                    scheduledAt.let { if (it == null) GtvNull else gtv(it) })

    /**
     * Operation economy_chain:propose_price_oracle_rate
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:propose_price_oracle_rate")
    fun TransactionBuilder.proposePriceOracleRateOperation(rates: List<PendingPriceOracleRateData>) =
            addOperation(PROPOSE_PRICE_ORACLE_RATE, gtv(rates.map { GtvObjectMapper.toGtvArray(it) }))

    /**
     * Operation economy_chain:propose_staking_requirement_constants
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:propose_staking_requirement_constants")
    fun TransactionBuilder.proposeStakingRequirementConstantsOperation(enabled: Boolean?,
                                                                       stopPayoutDays: Long?,
                                                                       systemNodeOwnStakeChr: Long?,
                                                                       systemNodeTotalStakeChr: Long?,
                                                                       dappNodeOwnStakeChr: Long?,
                                                                       dappNodeTotalStakeChr: Long?,
                                                                       scheduledAt: Long?) =
            addOperation(PROPOSE_STAKING_REQUIREMENT_CONSTANTS, enabled.let { if (it == null) GtvNull else gtv(it) },
                    stopPayoutDays.let { if (it == null) GtvNull else gtv(it) },
                    systemNodeOwnStakeChr.let { if (it == null) GtvNull else gtv(it) },
                    systemNodeTotalStakeChr.let { if (it == null) GtvNull else gtv(it) },
                    dappNodeOwnStakeChr.let { if (it == null) GtvNull else gtv(it) },
                    dappNodeTotalStakeChr.let { if (it == null) GtvNull else gtv(it) },
                    scheduledAt.let { if (it == null) GtvNull else gtv(it) })

    /**
     * Operation economy_chain:update_tag
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:update_tag")
    fun TransactionBuilder.updateTagOperationV57(name: String,
                                                 scuPrice: Long?,
                                                 extraStoragePrice: Long?) =
            addOperation(UPDATE_TAG, gtv(name),
                    scuPrice.let { if (it == null) GtvNull else gtv(it) },
                    extraStoragePrice.let { if (it == null) GtvNull else gtv(it) })

    /**
     * Operation common_proposal:revoke_common_proposal_v65
     *
     * Revoke common proposal providing voter set member key. This will be derived from the signer list.
     *
     * Permission: proposal owner
     *
     * Rate limit: no
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common_proposal:revoke_common_proposal_v65")
    fun TransactionBuilder.revokeCommonProposalV65Operation(proposalId: RowId) =
            addOperation(REVOKE_COMMON_PROPOSAL_V65, gtv(proposalId.id))

    /**
     * Operation common_proposal:make_common_vote_v65
     *
     * Make common vote without providing voter set member key. This will be derived from the signer list.
     *
     * Permission: common voter set member
     *
     * Rate limit: no
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "common_proposal:make_common_vote_v65")
    fun TransactionBuilder.makeCommonVoteV65Operation(proposalId: RowId,
                                                      vote: Boolean) =
            addOperation(MAKE_COMMON_VOTE_V65, gtv(proposalId.id),
                    gtv(vote))

    /**
     * Operation economy_chain:propose_system_provider_economy_constants
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:propose_system_provider_economy_constants")
    fun TransactionBuilder.proposeSystemProviderEconomyConstantsOperation(totalCostSystemProviders: Long?,
                                                                          systemProviderFeeShare: BigDecimal?,
                                                                          systemProviderRiskShare: BigDecimal?) =
            addOperation(PROPOSE_SYSTEM_PROVIDER_ECONOMY_CONSTANTS, totalCostSystemProviders.let { if (it == null) GtvNull else gtv(it) },
                    systemProviderFeeShare.let { if (it == null) GtvNull else gtv(it.toString()) },
                    systemProviderRiskShare.let { if (it == null) GtvNull else gtv(it.toString()) })
}