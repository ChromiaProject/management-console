package net.postchain.mc.cli.economy

import net.postchain.common.hexStringToByteArray
import net.postchain.economy.economy_chain.proposeStakingRequirementConstantsOperation
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandUpdateStakingEconomyConstantsIT {

    @Test
    fun `set new staking requirements with provider identifier`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
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