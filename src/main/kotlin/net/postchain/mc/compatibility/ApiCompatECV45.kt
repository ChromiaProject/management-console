package net.postchain.mc.compatibility

import net.postchain.client.core.PostchainQuery
import net.postchain.common.types.RowId
import net.postchain.economy.economy_chain.GET_ECONOMY_CONSTANTS
import net.postchain.economy.economy_chain.GET_STAKING_REQUIREMENT_CONSTANTS_PROPOSAL
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.Nullable
import net.postchain.gtv.mapper.toObject
import java.math.BigDecimal
import javax.annotation.processing.Generated

object ApiCompatECV45 {

    /**
     * Struct economy_chain:pending_staking_requirement_constants_data
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:pending_staking_requirement_constants_data")
    data class PendingStakingRequirementConstantsDataV45(
            @Name("enabled") @Nullable val enabled: Boolean?,
            @Name("stop_payout_days") @Nullable val stopPayoutDays: Long?,
            @Name("system_provider_own_stake_chr") @Nullable val systemProviderOwnStakeChr: Long?,
            @Name("system_provider_total_stake_chr") @Nullable val systemProviderTotalStakeChr: Long?,
            @Name("dapp_provider_own_stake_chr") @Nullable val dappProviderOwnStakeChr: Long?,
            @Name("dapp_provider_total_stake_chr") @Nullable val dappProviderTotalStakeChr: Long?
    )

    /**
     * Query economy_chain:get_staking_requirement_constants_proposal
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:get_staking_requirement_constants_proposal")
    fun PostchainQuery.getStakingRequirementConstantsProposalV45(proposalId: RowId) =
            query(GET_STAKING_REQUIREMENT_CONSTANTS_PROPOSAL, gtv(mapOf("proposal_id" to gtv(proposalId.id)))).toObject<PendingStakingRequirementConstantsDataV45>()

    /**
     * Struct economy_chain:economy_constants_data
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:economy_constants_data")
    data class EconomyConstantsDataV45(
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
            @Name("staking_requirement_system_provider_own_stake_chr") val stakingRequirementSystemProviderOwnStakeChr: Long,
            @Name("staking_requirement_system_provider_total_stake_chr") val stakingRequirementSystemProviderTotalStakeChr: Long,
            @Name("staking_requirement_dapp_provider_own_stake_chr") val stakingRequirementDappProviderOwnStakeChr: Long,
            @Name("staking_requirement_dapp_provider_total_stake_chr") val stakingRequirementDappProviderTotalStakeChr: Long,
            @Name("bridge_lease_add_min_balance_usd") val bridgeLeaseAddMinBalanceUsd: BigDecimal
    )

    /**
     * Query economy_chain:get_economy_constants
     *
     * Get economy constants.
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:get_economy_constants")
    fun PostchainQuery.getEconomyConstantsV45() =
            query(GET_ECONOMY_CONSTANTS, gtv(mapOf())).toObject<EconomyConstantsDataV45>()
}