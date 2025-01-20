package net.postchain.mc.compatibility

import net.postchain.client.transaction.TransactionBuilder
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import java.math.BigDecimal
import javax.annotation.processing.Generated

object ApiCompatECV51 {

    const val UPDATE_ECONOMY_CONSTANTS_ECV51 = "update_economy_constants"
    /**
     * Operation economy_chain:update_economy_constants
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:update_economy_constants")
    fun TransactionBuilder.updateEconomyConstantsOperationECV51(minLeaseTimeWeeks: Long?,
                                                                maxLeaseTimeWeeks: Long?,
                                                                stakingRewardRate: BigDecimal?,
                                                                stakingRewardFeeShare: BigDecimal?,
                                                                chromiaFoundationFeeShare: BigDecimal?,
                                                                resourcePoolMarginFeeShare: BigDecimal?,
                                                                dappProviderRiskShare: BigDecimal?) =
            addOperation(UPDATE_ECONOMY_CONSTANTS_ECV51, minLeaseTimeWeeks.let { if (it == null) GtvNull else gtv(it) },
                    maxLeaseTimeWeeks.let { if (it == null) GtvNull else gtv(it) },
                    stakingRewardRate.let { if (it == null) GtvNull else gtv(it.toString()) },
                    stakingRewardFeeShare.let { if (it == null) GtvNull else gtv(it.toString()) },
                    chromiaFoundationFeeShare.let { if (it == null) GtvNull else gtv(it.toString()) },
                    resourcePoolMarginFeeShare.let { if (it == null) GtvNull else gtv(it.toString()) },
                    dappProviderRiskShare.let { if (it == null) GtvNull else gtv(it.toString()) })

    const val PROPOSE_STAKING_REQUIREMENT_CONSTANTS_ECV51 = "propose_staking_requirement_constants"
    /**
     * Operation economy_chain:propose_staking_requirement_constants
     *
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:propose_staking_requirement_constants")
    fun TransactionBuilder.proposeStakingRequirementConstantsOperationECV51(enabled: Boolean?,
                                                                            stopPayoutDays: Long?,
                                                                            systemNodeOwnStakeChr: Long?,
                                                                            systemNodeTotalStakeChr: Long?,
                                                                            dappNodeOwnStakeChr: Long?,
                                                                            dappNodeTotalStakeChr: Long?) =
            addOperation(PROPOSE_STAKING_REQUIREMENT_CONSTANTS_ECV51, enabled.let { if (it == null) GtvNull else gtv(it) },
                    stopPayoutDays.let { if (it == null) GtvNull else gtv(it) },
                    systemNodeOwnStakeChr.let { if (it == null) GtvNull else gtv(it) },
                    systemNodeTotalStakeChr.let { if (it == null) GtvNull else gtv(it) },
                    dappNodeOwnStakeChr.let { if (it == null) GtvNull else gtv(it) },
                    dappNodeTotalStakeChr.let { if (it == null) GtvNull else gtv(it) })
}