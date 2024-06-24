package net.postchain.mc.cli.economy

import com.chromia.build.tools.TestProcess
import net.postchain.gtv.GtvFactory.gtv
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandUpdateSystemProviderEconomyConstantsIT : ECTestBase() {

    @Test
    fun `test propose system provider constants - old EC version`(@TempDir dir: Path) =
            ecRestApiTest(dir, ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION - 1) { ecModel ->
                TestProcess.Builder("economy", "update-system-provider-constants", "--total-cost-system-providers=123", "--system-provider-fee-share=0.1", "--system-provider-risk-share=0.2")
                        .awaitCompletion(true)
                        .setWorkingDir(dir.toFile())
                        .start { _ ->
                            ecModel.assertSingleOp(
                                    "propose_system_provider_economy_constants",
                                    listOf(gtv(123), gtv("0.1"), gtv("0.2")))
                        }
            }

    @Test
    fun `test propose system provider constants - new EC version`(@TempDir dir: Path) =
            ecRestApiTest(dir, ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION) { ecModel ->
                TestProcess.Builder("economy", "update-system-provider-constants", "--total-cost-system-providers=123", "--system-provider-fee-share=0.1", "--system-provider-risk-share=0.2")
                        .awaitCompletion(true)
                        .setWorkingDir(dir.toFile())
                        .start { _ ->
                            ecModel.assertSingleOp(
                                    "propose_system_provider_economy_constants",
                                    listOf(gtv(123L * UNITS_PER_USD), gtv("0.1"), gtv("0.2")))
                        }
            }
}