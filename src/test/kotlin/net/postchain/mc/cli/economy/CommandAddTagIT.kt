package net.postchain.mc.cli.economy

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.postchain.common.hexStringToByteArray
import net.postchain.economy.economy_chain.createTagOperation
import net.postchain.mc.cli.base.ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.compatibility.ApiCompatECV57.createTagOperationV57
import net.postchain.mc.compatibility.ApiCompatECV66.createTagOperationECV66
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandAddTagIT {

    @Test
    fun `add tag with dollar - v32 - no decimal`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION - 1)
                .testCommand(CommandAddTag(),
                        "--name", "t1", "--scu-price", "1", "--extra-storage-price", "2"
                ) { _, api ->
                    api.getEcModel().assertCalledOps {
                        it.createTagOperationV57("t1", 1, 2)
                    }
                }
    }

    @Test
    fun `add tag with minor units - v33 - rejects decimal`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION - 1)
                .testCommand(CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.2", "--extra-storage-price", "2"
                ) { result, _ ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.stderr).contains("This version of Economy chain only support price in dollar (no minor/decimal units)")
                }
    }

    @Test
    fun `add tag with dollar and cent - v33 - accepts decimal`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION)
                .testCommand(CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789"
                ) { _, api ->
                    api.getEcModel().assertCalledOps {
                        it.createTagOperationV57("t1", 1234000, 5123456)
                    }
                }
    }

    @Test
    fun `compute request price - v66 - reject due to not supported`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION - 1)
                .testCommand(CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789"
                ) { _, api ->
                    api.getEcModel().assertCalledOps {
                        it.createTagOperationECV66(api.pubKey.hexStringToByteArray(), "t1",
                                1234000, 5123456)
                    }
                }
    }

    @Test
    fun `compute request price - v67 - default value`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION)
                .testCommand(CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789"
                ) { _, api ->
                    api.getEcModel().assertCalledOps {
                        it.createTagOperation(api.pubKey.hexStringToByteArray(), "t1",
                                1234000, 5123456, 0)
                    }
                }
    }

    @Test
    fun `compute request price - v67`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION)
                .testCommand(CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789", "--extra-compute-request-price", "1.15"
                ) { _, api ->
                    api.getEcModel().assertCalledOps {
                        it.createTagOperation(api.pubKey.hexStringToByteArray(), "t1",
                                1234000, 5123456, 1150000)
                    }
                }
    }
}