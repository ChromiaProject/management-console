package net.postchain.mc.cli.economy

import net.postchain.mc.cli.base.ECONOMY_CHAIN_DYNAMIC_STAKING_REWARD_SHARE_VERSION
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertLineValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandGetEconomyConstantsIT {

    @Test
    fun `list constants v63`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_DYNAMIC_STAKING_REWARD_SHARE_VERSION - 1)
                .testCommand(CommandGetEconomyConstants()) { result, _ ->
                    assertLineValue(result.stdout, "Min_lease_time_weeks", "1")
                    assertLineValue(result.stdout, "Max_lease_time_weeks", "10")
                    assertLineValue(result.stdout, "CHR_per_USD", "5.1")
                    assertLineValue(result.stdout, "Total_cost_system_providers_in_USD", "800.123456")
                    assertLineValue(result.stdout, "System_provider_fee_share", "0.1")
                    assertLineValue(result.stdout, "Staking_reward_rate", "0.2")
                    assertLineValue(result.stdout, "Staking_reward_fee_share", "0.3")
                    assertLineValue(result.stdout, "Chromia_foundation_fee_share", "0.4")
                    assertLineValue(result.stdout, "Resource_pool_margin_fee_share", "0.5")
                    assertLineValue(result.stdout, "System_provider_risk_share", "0.6")
                    assertLineValue(result.stdout, "Dapp_provider_risk_share", "0.7")
                    assertLineValue(result.stdout, "Staking_requirements_enabled", "true")
                    assertLineValue(result.stdout, "Staking_requirements_stop_payout_days", "14")
                    assertLineValue(result.stdout, "Staking_requirements_system_node_own", "1.123456")
                    assertLineValue(result.stdout, "Staking_requirements_system_node_total", "2.123456")
                    assertLineValue(result.stdout, "Staking_requirements_dapp_node_own", "3.123456")
                    assertLineValue(result.stdout, "Staking_requirements_dapp_node_total", "4.123456")
                    assertLineValue(result.stdout, "Bridge_lease_add_min_balance_in_USD", "100")
                }
    }

    @Test
    fun `list constants v64`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_DYNAMIC_STAKING_REWARD_SHARE_VERSION)
                .testCommand(CommandGetEconomyConstants()) { result, _ ->
                    assertLineValue(result.stdout, "Min_lease_time_weeks", "1")
                    assertLineValue(result.stdout, "Max_lease_time_weeks", "10")
                    assertLineValue(result.stdout, "CHR_per_USD", "5.1")
                    assertLineValue(result.stdout, "Total_cost_system_providers_in_USD", "800.123456")
                    assertLineValue(result.stdout, "System_provider_fee_share", "0.1")
                    assertLineValue(result.stdout, "Staking_reward_rate", "0.2")
                    assertLineValue(result.stdout, "Chromia_foundation_fee_share", "0.4")
                    assertLineValue(result.stdout, "Resource_pool_margin_fee_share", "0.5")
                    assertLineValue(result.stdout, "System_provider_risk_share", "0.6")
                    assertLineValue(result.stdout, "Dapp_provider_risk_share", "0.7")
                    assertLineValue(result.stdout, "Staking_requirements_enabled", "true")
                    assertLineValue(result.stdout, "Staking_requirements_stop_payout_days", "14")
                    assertLineValue(result.stdout, "Staking_requirements_system_node_own", "1.123456")
                    assertLineValue(result.stdout, "Staking_requirements_system_node_total", "2.123456")
                    assertLineValue(result.stdout, "Staking_requirements_dapp_node_own", "3.123456")
                    assertLineValue(result.stdout, "Staking_requirements_dapp_node_total", "4.123456")
                    assertLineValue(result.stdout, "Bridge_lease_add_min_balance_in_USD", "100")
                }
    }
}