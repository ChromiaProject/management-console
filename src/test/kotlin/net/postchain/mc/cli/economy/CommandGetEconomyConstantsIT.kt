package net.postchain.mc.cli.economy

import com.chromia.build.tools.TestProcess
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandGetEconomyConstantsIT : ECTestBase() {

    @Test
    fun `list constants - legacy EC version`(@TempDir dir: Path) =
            ecRestApiTest(dir, ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION - 1) {
                TestProcess.Builder("economy", "get-constants")
                        .awaitCompletion(true)
                        .setWorkingDir(dir.toFile())
                        .start { testProcess ->
                            val lines = testProcess.readLines()
                            assertLineValue(lines, "Min_lease_time_weeks", "1")
                            assertLineValue(lines, "Max_lease_time_weeks", "10")
                            assertLineValue(lines, "CHR_per_USD", "5.1")
                            assertLineValue(lines, "Total_cost_system_providers", "800123456")
                            assertLineValue(lines, "System_provider_fee_share", "0.1")
                            assertLineValue(lines, "Staking_reward_rate", "0.2")
                            assertLineValue(lines, "Staking_reward_fee_share", "0.3")
                            assertLineValue(lines, "Chromia_foundation_fee_share", "0.4")
                            assertLineValue(lines, "Resource_pool_margin_fee_share", "0.5")
                            assertLineValue(lines, "System_provider_risk_share", "0.6")
                            assertLineValue(lines, "Staking_requirements_enabled", "true")
                            assertLineValue(lines, "Staking_requirements_stop_payout_days", "14")
                            assertLineValue(lines, "Staking_requirements_system_provider_own", "1123456")
                            assertLineValue(lines, "Staking_requirements_system_provider_total", "2123456")
                            assertLineValue(lines, "Staking_requirements_dapp_provider_own", "3123456")
                            assertLineValue(lines, "Staking_requirements_dapp_provider_total", "4123456")
                        }
            }

    @Test
    fun `list constants - new EC version`(@TempDir dir: Path) =
            ecRestApiTest(dir, ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION) {
                TestProcess.Builder("economy", "get-constants")
                        .awaitCompletion(true)
                        .setWorkingDir(dir.toFile())
                        .start { testProcess ->
                            val lines = testProcess.readLines()
                            assertLineValue(lines, "Min_lease_time_weeks", "1")
                            assertLineValue(lines, "Max_lease_time_weeks", "10")
                            assertLineValue(lines, "CHR_per_USD", "5.1")
                            assertLineValue(lines, "Total_cost_system_providers", "800.123456")
                            assertLineValue(lines, "System_provider_fee_share", "0.1")
                            assertLineValue(lines, "Staking_reward_rate", "0.2")
                            assertLineValue(lines, "Staking_reward_fee_share", "0.3")
                            assertLineValue(lines, "Chromia_foundation_fee_share", "0.4")
                            assertLineValue(lines, "Resource_pool_margin_fee_share", "0.5")
                            assertLineValue(lines, "System_provider_risk_share", "0.6")
                            assertLineValue(lines, "Staking_requirements_enabled", "true")
                            assertLineValue(lines, "Staking_requirements_stop_payout_days", "14")
                            assertLineValue(lines, "Staking_requirements_system_provider_own", "1.123456")
                            assertLineValue(lines, "Staking_requirements_system_provider_total", "2.123456")
                            assertLineValue(lines, "Staking_requirements_dapp_provider_own", "3.123456")
                            assertLineValue(lines, "Staking_requirements_dapp_provider_total", "4.123456")
                        }
            }
}