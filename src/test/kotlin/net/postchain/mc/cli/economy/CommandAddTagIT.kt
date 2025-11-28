package net.postchain.mc.cli.economy

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.postchain.common.hexStringToByteArray
import net.postchain.economy.economy_chain.createTagOperation
import net.postchain.mc.cli.base.ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.compatibility.ApiCompatECV66.createTagOperationECV66
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandAddTagIT {

    @Test
    fun `add tag - v66`(@TempDir dir: Path) {
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
    fun `compute request price - v66 - reject due to not supported`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION - 1)
                .testCommand(CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789",
                        "--extra-compute-request-price", "1.15"
                ) { result, api ->
                    api.getEcModel().assertCalledOps {
                        assertThat(result.statusCode).isEqualTo(1)
                        assertThat(result.stderr).contains("This version of Economy chain does not support extra compute request price on tags")
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