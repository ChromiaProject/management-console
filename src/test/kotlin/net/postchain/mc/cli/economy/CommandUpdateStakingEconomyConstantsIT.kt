package net.postchain.mc.cli.economy

import assertk.assertThat
import assertk.assertions.isEqualTo
import net.postchain.common.hexStringToByteArray
import net.postchain.economy.economy_chain.proposeStakingRequirementConstantsOperation
import net.postchain.mc.cli.base.ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_STAKING_REQUIREMENTS_VERSION
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.compatibility.ApiCompatECV57.proposeStakingRequirementConstantsOperation
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandUpdateStakingEconomyConstantsIT {

    @Test
    fun `unsupported EC version`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_STAKING_REQUIREMENTS_VERSION - 1)
                .testCommand(CommandUpdateStakingEconomyConstants(),
                        "--staking-requirements-enabled=true",
                        "--staking-requirements-stop-payout-days=5",
                        "--staking-requirements-sn-own=1000",
                        "--staking-requirements-sn-total=1300",
                        "--staking-requirements-dn-own=500",
                        "--staking-requirements-dn-total=800"
                ) { result, _ ->
                    println(result.output)
                    assertThat(result.stdout.trim()).isEqualTo("Command not supported by network. Requires version 24, but was 23")
                }
    }

    @Test
    fun `set new staking requirements`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION - 1)
                .testCommand(CommandUpdateStakingEconomyConstants(),
                        "--staking-requirements-enabled=true",
                        "--staking-requirements-stop-payout-days=5",
                        "--staking-requirements-sn-own=1000",
                        "--staking-requirements-sn-total=1300",
                        "--staking-requirements-dn-own=500",
                        "--staking-requirements-dn-total=800"
                ) { _, api ->
                    api.getEcModel().assertCalledOps {
                        it.proposeStakingRequirementConstantsOperation(true, 5, 1000L.times(UNITS_PER_CHR), 1300L.times(UNITS_PER_CHR), 500L.times(UNITS_PER_CHR), 800L.times(UNITS_PER_CHR), null)
                    }
                }
    }

    @Test
    fun `set new staking requirements with scheduled at`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION - 1)
                .testCommand(CommandUpdateStakingEconomyConstants(),
                        "--staking-requirements-enabled=true",
                        "--staking-requirements-stop-payout-days=5",
                        "--staking-requirements-sn-own=1000",
                        "--staking-requirements-sn-total=1300",
                        "--staking-requirements-dn-own=500",
                        "--staking-requirements-dn-total=800",
                        "--schedule-at=2025-04-16T19:10"
                ) { _, api ->
                    api.getEcModel().assertCalledOps {
                        it.proposeStakingRequirementConstantsOperation(true, 5, 1000L.times(UNITS_PER_CHR), 1300L.times(UNITS_PER_CHR), 500L.times(UNITS_PER_CHR), 800L.times(UNITS_PER_CHR), 1744830600000)
                    }
                }
    }

    @Test
    fun `set new staking requirements with provider identifier`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION)
                .testCommand(CommandUpdateStakingEconomyConstants(),
                        "--staking-requirements-enabled=true",
                        "--staking-requirements-stop-payout-days=5",
                        "--staking-requirements-sn-own=1000",
                        "--staking-requirements-sn-total=1300",
                        "--staking-requirements-dn-own=500",
                        "--staking-requirements-dn-total=800",
                        "--schedule-at=2025-04-16T19:10"
                ) { _, api ->
                    api.getEcModel().assertCalledOps {
                        it.proposeStakingRequirementConstantsOperation(api.pubKey.hexStringToByteArray(), true, 5, 1000L.times(UNITS_PER_CHR), 1300L.times(UNITS_PER_CHR), 500L.times(UNITS_PER_CHR), 800L.times(UNITS_PER_CHR), 1744830600000)
                    }
                }
    }
}