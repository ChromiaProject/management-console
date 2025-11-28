package net.postchain.mc.cli.economy

import net.postchain.gtv.GtvArray
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.base.ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertLineValue
import net.postchain.mc.compatibility.ApiCompatECV66
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandListTagsIT {

    @Test
    fun `list tags with correct usd value - v32`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION - 1)
                .withECQuery("get_tags", GtvArray(arrayOf(GtvObjectMapper.toGtvDictionary(
                        ApiCompatECV66.TagDataECV66("t1", 1, 2)))))
                .testCommand(CommandListTags()) { result, _ ->
                    assertLineValue(result.stdout, "SCU_price", "1")
                    assertLineValue(result.stdout, "Extra_storage_price", "2")
                }
    }

    @Test
    fun `list tags with correct usd value - v33`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION)
                .withECQuery("get_tags", GtvArray(arrayOf(GtvObjectMapper.toGtvDictionary(
                        ApiCompatECV66.TagDataECV66("t1", 1, 2)))))
                .testCommand(CommandListTags()) { result, _ ->
                    assertLineValue(result.stdout, "SCU_price", "0.000001")
                    assertLineValue(result.stdout, "Extra_storage_price", "0.000002")
                }
    }

    @Test
    fun `list tags with correct usd value - v67`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION)
                .testCommand(CommandListTags()) { result, _ ->
                    assertLineValue(result.stdout, "SCU_price", "0.000001")
                    assertLineValue(result.stdout, "Extra_storage_price", "0.000002")
                    assertLineValue(result.stdout, "Extra_compute_request_price", "0.000003")
                }
    }
}