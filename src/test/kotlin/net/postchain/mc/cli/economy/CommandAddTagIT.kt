package net.postchain.mc.cli.economy

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandAddTagIT {

    @Test
    fun `add tag with dollar - legacy EC version`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION - 1)
                .testCommand(CommandAddTag(),
                        "--name", "t1", "--scu-price", "1", "--extra-storage-price", "2"
                ) { result, api ->
                    api.getEcModel().assertSingleOp("create_tag", listOf(gtv("t1"), gtv(1), gtv(2)))
                }
    }

    @Test
    fun `add tag with minor units - legacy EC version`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION - 1)
                .testCommand(CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.2", "--extra-storage-price", "2"
                ) { result, api ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.stderr).contains("This version of Economy chain only support price in dollar (no minor/decimal units)")
                }
    }

    @Test
    fun `add tag with dollar and cent - new EC version`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION)
                .testCommand(CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789"
                ) { result, api ->
                    api.getEcModel().assertSingleOp("create_tag", listOf(gtv("t1"), gtv(1234000), gtv(5123456)))
                }
    }
}