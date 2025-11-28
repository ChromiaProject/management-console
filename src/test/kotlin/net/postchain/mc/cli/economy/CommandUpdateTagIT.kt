package net.postchain.mc.cli.economy

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.postchain.common.hexStringToByteArray
import net.postchain.economy.economy_chain.updateTagOperation
import net.postchain.mc.cli.base.ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.compatibility.ApiCompatECV66.updateTagOperationECV66
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandUpdateTagIT {

    @Test
    fun `reject if no price arguments`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .testCommand(CommandUpdateTag(), "--name", "t1") { result, _ ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.stderr).contains("Must specify either SCU price, extra storage price or extra compute request price")
                }
    }

    @Test
    fun `rejects compute argument if v66`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION - 1)
                .testCommand(CommandUpdateTag(),
                        "--name", "t1", "--extra-compute-request-price", "2"
                ) { result, _ ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.stderr).contains("This version of Economy chain does not support extra compute request price on tags")
                }
    }

    @Test
    fun `successful on v66`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION - 1)
                .testCommand(CommandUpdateTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789",
                ) { _, api ->
                    api.getEcModel().assertCalledOps {
                        it.updateTagOperationECV66(api.pubKey.hexStringToByteArray(), "t1", 1234000, 5123456)
                    }
                }
    }

    @Test
    fun `successful on v67 with compute request price`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION)
                .testCommand(CommandUpdateTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789",
                        "--extra-compute-request-price", "1.15"
                ) { _, api ->
                    api.getEcModel().assertCalledOps {
                        it.updateTagOperation(api.pubKey.hexStringToByteArray(), "t1", 1234000, 5123456, 1150000)
                    }
                }
    }
}