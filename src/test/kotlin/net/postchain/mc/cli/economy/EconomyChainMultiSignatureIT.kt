package net.postchain.mc.cli.economy

import assertk.assertThat
import assertk.assertions.isEmpty
import com.github.ajalt.mordant.input.KeyboardEvent
import net.postchain.common.hexStringToByteArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PUBKEY_1
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PUBKEY_2
import net.postchain.mc.cli.test_helpers.DEFAULT_BRID_ECONOMY_CHAIN
import net.postchain.mc.cli.test_helpers.DEFAULT_DAPP_RID
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandFailureContains
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import net.postchain.mc.cli.test_helpers.assertSavedTransaction
import net.postchain.mc.cli.test_helpers.writeResourceFileToTempDir
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString

class EconomyChainMultiSignatureIT {

    @Test
    fun `send transaction directly`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .testCommand(
                        CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789",
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertCommandSuccessContains(result, "Proposal for creating tag t1 is created")
                    api.getEcModel().assertSingleOp("create_tag", listOf(gtv(DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray()), gtv("t1"), gtv(1234000), gtv(5123456)))
                }
    }

    @Test
    fun `save always`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .testCommand(
                        CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789",
                        "--save-tx",
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertSavedTransaction(result, dir, DEFAULT_BRID_ECONOMY_CHAIN, listOf(DEFAULT_PROVIDER01_PUBKEY), listOf())
                    assertThat(api.getEcModel().capturedOps).isEmpty()
                }
    }

    @Test
    fun `save transaction to file with signer options`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .testCommand(
                        CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789",
                        "--signer", ADDITIONAL_PUBKEY_1,
                        "--signer", ADDITIONAL_PUBKEY_2,
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertSavedTransaction(result, dir, DEFAULT_BRID_ECONOMY_CHAIN, listOf(DEFAULT_PROVIDER01_PUBKEY), listOf(ADDITIONAL_PUBKEY_1, ADDITIONAL_PUBKEY_2))
                    assertThat(api.getEcModel().capturedOps).isEmpty()
                }
    }

    @Test
    fun `save transaction to file with signers option`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .testCommand(
                        CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789",
                        "--signers", "$ADDITIONAL_PUBKEY_1,$ADDITIONAL_PUBKEY_2",
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertSavedTransaction(result, dir, DEFAULT_BRID_ECONOMY_CHAIN, listOf(DEFAULT_PROVIDER01_PUBKEY), listOf(ADDITIONAL_PUBKEY_1, ADDITIONAL_PUBKEY_2))
                    assertThat(api.getEcModel().capturedOps).isEmpty()
                }
    }

    @Test
    fun `save transaction to file with signers file`(@TempDir dir: Path) {
        val signers = writeResourceFileToTempDir(dir, "/signers")
        ManagedRestTestApi(dir)
                .testCommand(
                        CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789",
                        "--signers-file", signers.absolutePath.toString(),
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertSavedTransaction(result, dir, DEFAULT_BRID_ECONOMY_CHAIN, listOf(DEFAULT_PROVIDER01_PUBKEY), listOf(ADDITIONAL_PUBKEY_1, ADDITIONAL_PUBKEY_2))
                    assertThat(api.getEcModel().capturedOps).isEmpty()
                }
    }

    @Test
    fun `save transaction to file with signers DC config`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_provider_keys_and_threshold",
                        gtv(mapOf("keys" to gtv(listOf(
                                gtv(DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_1.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_2.hexStringToByteArray()),
                        )), "threshold" to gtv(3))))
                .testCommand(
                        CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789",
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertSavedTransaction(result, dir, DEFAULT_BRID_ECONOMY_CHAIN, listOf(DEFAULT_PROVIDER01_PUBKEY), listOf(ADDITIONAL_PUBKEY_1, ADDITIONAL_PUBKEY_2))
                    assertThat(api.getEcModel().capturedOps).isEmpty()
                }
    }

    @Test
    fun `ambiguous configuration from DC`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_provider_keys_and_threshold",
                        gtv(mapOf("keys" to gtv(listOf(
                                gtv(DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_1.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_2.hexStringToByteArray()),
                        )), "threshold" to gtv(2))))
                .testCommand(
                        CommandAddTag(),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789",
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertCommandFailureContains(result, "Transaction needs to be signed by 2 keys of [$DEFAULT_PROVIDER01_PUBKEY, $ADDITIONAL_PUBKEY_1, $ADDITIONAL_PUBKEY_2], please specify which keys to use with --signers option")
                    assertThat(api.getEcModel().capturedOps).isEmpty()
                }
    }

    @Timeout(10, unit = TimeUnit.SECONDS)
    @Test
    fun `ambiguous configuration from DC with manual selection success`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_provider_keys_and_threshold",
                        gtv(mapOf("keys" to gtv(listOf(
                                gtv(DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_1.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_2.hexStringToByteArray()),
                        )), "threshold" to gtv(2))))
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testInteractiveCommand(
                        CommandAddTag(),
                        listOf(
                                KeyboardEvent("ArrowDown"),
                                KeyboardEvent("x"),
                                KeyboardEvent("Enter"),
                        ),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789",
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertSavedTransaction(result, dir, DEFAULT_BRID_ECONOMY_CHAIN, listOf(DEFAULT_PROVIDER01_PUBKEY), listOf(ADDITIONAL_PUBKEY_2))
                    assertThat(api.getEcModel().capturedOps).isEmpty()
                }
    }

    @Timeout(10, unit = TimeUnit.SECONDS)
    @Test
    fun `ambiguous configuration from DC with manual selection failure`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_provider_keys_and_threshold",
                        gtv(mapOf("keys" to gtv(listOf(
                                gtv(DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_1.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_2.hexStringToByteArray()),
                        )), "threshold" to gtv(2))))
                .testInteractiveCommand(
                        CommandAddTag(),
                        listOf(
                                KeyboardEvent("Enter"),
                        ),
                        "--name", "t1", "--scu-price", "1.234", "--extra-storage-price", "5.123456789",
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertCommandFailureContains(result, "You need to select 1 keys, only 0 was selected")
                    assertThat(api.getEcModel().capturedOps).isEmpty()
                }
    }
}
