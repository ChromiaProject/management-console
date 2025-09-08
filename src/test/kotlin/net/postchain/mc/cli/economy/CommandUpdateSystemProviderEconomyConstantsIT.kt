package net.postchain.mc.cli.economy

import net.postchain.common.hexStringToByteArray
import net.postchain.economy.economy_chain.proposeSystemProviderEconomyConstantsOperation
import net.postchain.mc.cli.base.ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.compatibility.ApiCompatECV57.proposeSystemProviderEconomyConstantsOperation
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.math.BigDecimal
import java.nio.file.Path

class CommandUpdateSystemProviderEconomyConstantsIT {

    @Test
    fun `test propose system provider constants - old EC version`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION - 1)
                .testCommand(CommandUpdateSystemProviderEconomyConstants(), "--total-cost-system-providers=123",
                        "--system-provider-fee-share=0.1", "--system-provider-risk-share=0.2") { _, api ->
                    api.getEcModel().assertCalledOps {
                        it.proposeSystemProviderEconomyConstantsOperation(123, BigDecimal("0.1"), BigDecimal("0.2"))
                    }
                }
    }

    @Test
    fun `test propose system provider constants - new EC version`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION)
                .testCommand(CommandUpdateSystemProviderEconomyConstants(), "--total-cost-system-providers=123",
                        "--system-provider-fee-share=0.1", "--system-provider-risk-share=0.2") { _, api ->
                    api.getEcModel().assertCalledOps {
                        it.proposeSystemProviderEconomyConstantsOperation(123L * UNITS_PER_USD, BigDecimal("0.1"), BigDecimal("0.2"))
                    }
                }
    }

    @Test
    fun `test propose system provider constants - require provider identifier version`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION)
                .testCommand(CommandUpdateSystemProviderEconomyConstants(), "--total-cost-system-providers=123",
                        "--system-provider-fee-share=0.1", "--system-provider-risk-share=0.2") { _, api ->
                    api.getEcModel().assertCalledOps {
                        it.proposeSystemProviderEconomyConstantsOperation(api.pubKey.hexStringToByteArray(),123L * UNITS_PER_USD, BigDecimal("0.1"), BigDecimal("0.2"))
                    }
                }
    }
}