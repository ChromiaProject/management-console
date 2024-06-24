package net.postchain.mc.cli.economy

import com.chromia.build.tools.TestProcess
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandListTagsIT : ECTestBase() {

    @Test
    fun `list tags with correct usd value - legacy EC version`(@TempDir dir: Path) =
            ecRestApiTest(dir, ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION - 1) {
                TestProcess.Builder("economy", "tags")
                        .awaitCompletion(true)
                        .setWorkingDir(dir.toFile())
                        .start { testProcess ->
                            val lines = testProcess.readLines()
                            assertLineValue(lines, "SCU_price", "1")
                            assertLineValue(lines, "Extra_storage_price", "2")
                        }
            }

    @Test
    fun `list tags with correct usd value - new EC version`(@TempDir dir: Path) =
            ecRestApiTest(dir, ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION) {
                TestProcess.Builder("economy", "tags")
                        .awaitCompletion(true)
                        .setWorkingDir(dir.toFile())
                        .start { testProcess ->
                            val lines = testProcess.readLines()
                            assertLineValue(lines, "SCU_price", "0.000001")
                            assertLineValue(lines, "Extra_storage_price", "0.000002")
                        }
            }
}