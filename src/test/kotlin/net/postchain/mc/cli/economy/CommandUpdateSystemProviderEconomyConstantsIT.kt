package net.postchain.mc.cli.economy

import net.postchain.common.hexStringToByteArray
import net.postchain.economy.economy_chain.proposeSystemProviderEconomyConstantsOperation
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.math.BigDecimal
import java.nio.file.Path

class CommandUpdateSystemProviderEconomyConstantsIT {

    @Test
    fun `test propose system provider constants`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .testCommand(CommandUpdateSystemProviderEconomyConstants(), "--total-cost-system-providers=123",
                        "--system-provider-fee-share=0.1", "--system-provider-risk-share=0.2") { _, api ->
                    api.getEcModel().assertCalledOps {
                        it.proposeSystemProviderEconomyConstantsOperation(api.pubKey.hexStringToByteArray(),123L * UNITS_PER_USD, BigDecimal("0.1"), BigDecimal("0.2"))
                    }
                }
    }
}