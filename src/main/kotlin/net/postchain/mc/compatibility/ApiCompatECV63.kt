package net.postchain.mc.compatibility

import net.postchain.client.core.PostchainQuery
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.types.RowId
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.Nullable
import net.postchain.gtv.mapper.toObject
import java.math.BigDecimal
import javax.annotation.processing.Generated

object ApiCompatECV63 {
    const val UPDATE_ECONOMY_CONSTANTS = "update_economy_constants"

    /**
     * Operation economy_chain:update_economy_constants
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:update_economy_constants")
    fun TransactionBuilder.updateEconomyConstantsOperationECV63(myPubkey: ByteArray,
                                                                minLeaseTimeWeeks: Long?,
                                                                maxLeaseTimeWeeks: Long?,
                                                                stakingRewardRate: BigDecimal?,
                                                                stakingRewardFeeShare: BigDecimal?,
                                                                chromiaFoundationFeeShare: BigDecimal?,
                                                                resourcePoolMarginFeeShare: BigDecimal?,
                                                                dappProviderRiskShare: BigDecimal?,
                                                                scheduledAt: Long?) =
            addOperation(UPDATE_ECONOMY_CONSTANTS, gtv(myPubkey),
                    minLeaseTimeWeeks.let { if (it == null) GtvNull else gtv(it) },
                    maxLeaseTimeWeeks.let { if (it == null) GtvNull else gtv(it) },
                    stakingRewardRate.let { if (it == null) GtvNull else gtv(it.toString()) },
                    stakingRewardFeeShare.let { if (it == null) GtvNull else gtv(it.toString()) },
                    chromiaFoundationFeeShare.let { if (it == null) GtvNull else gtv(it.toString()) },
                    resourcePoolMarginFeeShare.let { if (it == null) GtvNull else gtv(it.toString()) },
                    dappProviderRiskShare.let { if (it == null) GtvNull else gtv(it.toString()) },
                    scheduledAt.let { if (it == null) GtvNull else gtv(it) })

    /**
     * Struct economy_chain:economy_constants_data
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:economy_constants_data")
    data class EconomyConstantsDataECV63(
            @Name("min_lease_time_weeks") val minLeaseTimeWeeks: Long,
            @Name("max_lease_time_weeks") val maxLeaseTimeWeeks: Long,
            @Name("chr_per_usd") val chrPerUsd: BigDecimal,
            @Name("total_cost_system_providers") val totalCostSystemProviders: Long,
            @Name("system_provider_fee_share") val systemProviderFeeShare: BigDecimal,
            @Name("staking_reward_rate") val stakingRewardRate: BigDecimal,
            @Name("staking_reward_fee_share") val stakingRewardFeeShare: BigDecimal,
            @Name("chromia_foundation_fee_share") val chromiaFoundationFeeShare: BigDecimal,
            @Name("resource_pool_margin_fee_share") val resourcePoolMarginFeeShare: BigDecimal,
            @Name("system_provider_risk_share") val systemProviderRiskShare: BigDecimal,
            @Name("dapp_provider_risk_share") val dappProviderRiskShare: BigDecimal,
            @Name("staking_requirements_enabled") val stakingRequirementsEnabled: Boolean,
            @Name("staking_requirement_stop_payout_days") val stakingRequirementStopPayoutDays: Long,
            @Name("staking_requirement_system_node_own_stake_chr") val stakingRequirementSystemNodeOwnStakeChr: Long,
            @Name("staking_requirement_system_node_total_stake_chr") val stakingRequirementSystemNodeTotalStakeChr: Long,
            @Name("staking_requirement_dapp_node_own_stake_chr") val stakingRequirementDappNodeOwnStakeChr: Long,
            @Name("staking_requirement_dapp_node_total_stake_chr") val stakingRequirementDappNodeTotalStakeChr: Long,
            @Name("bridge_lease_add_min_balance_usd") val bridgeLeaseAddMinBalanceUsd: BigDecimal
    )

    const val GET_ECONOMY_CONSTANTS = "get_economy_constants"

    /**
     * Query economy_chain:get_economy_constants
     *
     * Get economy constants.
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:get_economy_constants")
    fun PostchainQuery.getEconomyConstantsECV63() =
            query(GET_ECONOMY_CONSTANTS, gtv(mapOf())).toObject<EconomyConstantsDataECV63>()

    /**
     * Struct economy_chain:pending_economy_constants_data
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:pending_economy_constants_data")
    data class PendingEconomyConstantsDataECV63(
            @Name("min_lease_time_weeks") @Nullable val minLeaseTimeWeeks: Long?,
            @Name("max_lease_time_weeks") @Nullable val maxLeaseTimeWeeks: Long?,
            @Name("staking_reward_rate") @Nullable val stakingRewardRate: BigDecimal?,
            @Name("staking_reward_fee_share") @Nullable val stakingRewardFeeShare: BigDecimal?,
            @Name("chromia_foundation_fee_share") @Nullable val chromiaFoundationFeeShare: BigDecimal?,
            @Name("resource_pool_margin_fee_share") @Nullable val resourcePoolMarginFeeShare: BigDecimal?,
            @Name("dapp_provider_risk_share") @Nullable val dappProviderRiskShare: BigDecimal?
    )

    const val GET_ECONONY_CONSTANTS_PROPOSAL = "get_econony_constants_proposal"

    /**
     * Query economy_chain:get_econony_constants_proposal
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:get_econony_constants_proposal")
    fun PostchainQuery.getEcononyConstantsProposalECV63(proposalId: RowId) =
            query(GET_ECONONY_CONSTANTS_PROPOSAL, gtv(mapOf("proposal_id" to gtv(proposalId.id)))).toObject<PendingEconomyConstantsDataECV63>()

}
