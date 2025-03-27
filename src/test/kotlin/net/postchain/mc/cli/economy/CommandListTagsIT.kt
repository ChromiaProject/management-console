package net.postchain.mc.cli.economy

import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertLineValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandListTagsIT {

    @Test
    fun `list tags with correct usd value - legacy EC version`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION - 1)
                .testCommand(CommandListTags()) { result, _ ->
                    assertLineValue(result.stdout, "SCU_price", "1")
                    assertLineValue(result.stdout, "Extra_storage_price", "2")
                }
    }

    @Test
    fun `list tags with correct usd value - new EC version`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION)
                .testCommand(CommandListTags()) { result, _ ->
                    assertLineValue(result.stdout, "SCU_price", "0.000001")
                    assertLineValue(result.stdout, "Extra_storage_price", "0.000002")
                }
    }
}