package net.postchain.mc.compatibility

import net.postchain.client.transaction.TransactionBuilder
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import java.math.BigDecimal
import javax.annotation.processing.Generated

object ApiCompatECV28 {

    /**
     * Operation economy_chain:update_staking_requirements_economy_constants
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:update_staking_requirements_economy_constants")
    fun TransactionBuilder.updateStakingRequirementsEconomyConstantsOperationECV28(stakingRequirementsEnabled: Boolean?,
                                                                                   stakingRequirementStopPayoutDays: Long?,
                                                                                   stakingRequirementSystemProviderOwnStakeChr: Long?,
                                                                                   stakingRequirementSystemProviderTotalStakeChr: Long?,
                                                                                   stakingRequirementDappProviderOwnStakeChr: Long?,
                                                                                   stakingRequirementDappProviderTotalStakeChr: Long?) =
            addOperation("update_staking_requirements_economy_constants", stakingRequirementsEnabled.let { if (it == null) GtvNull else gtv(it) },
                    stakingRequirementStopPayoutDays.let { if (it == null) GtvNull else gtv(it) },
                    stakingRequirementSystemProviderOwnStakeChr.let { if (it == null) GtvNull else gtv(it) },
                    stakingRequirementSystemProviderTotalStakeChr.let { if (it == null) GtvNull else gtv(it) },
                    stakingRequirementDappProviderOwnStakeChr.let { if (it == null) GtvNull else gtv(it) },
                    stakingRequirementDappProviderTotalStakeChr.let { if (it == null) GtvNull else gtv(it) })

    /**
     * Operation economy_chain:update_system_provider_economy_constants
     */
    @Generated("net.postchain.rell.codegen.CodeGenerator", comments = "economy_chain:update_system_provider_economy_constants")
    fun TransactionBuilder.updateSystemProviderEconomyConstantsOperationECV28(totalCostSystemProviders: Long?,
                                                                              systemProviderFeeShare: BigDecimal?,
                                                                              systemProviderRiskShare: BigDecimal?) =
            addOperation("update_system_provider_economy_constants", totalCostSystemProviders.let { if (it == null) GtvNull else gtv(it) },
                    systemProviderFeeShare.let { if (it == null) GtvNull else gtv(it.toString()) },
                    systemProviderRiskShare.let { if (it == null) GtvNull else gtv(it.toString()) })
}