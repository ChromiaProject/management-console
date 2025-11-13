package net.postchain.mc.cli

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isTrue
import com.github.ajalt.mordant.input.KeyboardEvent
import net.postchain.common.hexStringToByteArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.blockchain.CommandProposeBlockchain
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PUBKEY_1
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PUBKEY_2
import net.postchain.mc.cli.test_helpers.DEFAULT_DAPP_RID
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.addDcEmptyListQueries
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

class DirectoryChainMultiSignatureIT {

    @Test
    fun `send transaction directly`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir)
                .addDcEmptyListQueries("get_compressed_configuration_parts")
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testCommand(
                        CommandProposeBlockchain(),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                ) { result, api ->
                    assertCommandSuccessContains(result, "Blockchain name01 has been added, bc-rid: ${DEFAULT_DAPP_RID.toHex()}")

                    assertThat(api.getDcModel().opWasCalled("propose_blockchain") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().isNotEmpty() &&
                                it[2].asString() == "name01" &&
                                it[3].asString() == "container01" &&
                                it[4].asString() == "Add blockchain name01 to the container container01"
                    }).isTrue()
                }
    }

    @Test
    fun `save transaction to file with signer options`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir)
                .addDcEmptyListQueries("get_compressed_configuration_parts")
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testCommand(
                        CommandProposeBlockchain(),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                        "--signer", ADDITIONAL_PUBKEY_1,
                        "--signer", ADDITIONAL_PUBKEY_2,
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertSavedTransaction(result, dir, listOf(DEFAULT_PROVIDER01_PUBKEY), listOf(ADDITIONAL_PUBKEY_1, ADDITIONAL_PUBKEY_2))
                    assertThat(api.getDcModel().capturedOps).isEmpty()
                }
    }

    @Test
    fun `save transaction to file with signers option`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir)
                .addDcEmptyListQueries("get_compressed_configuration_parts")
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testCommand(
                        CommandProposeBlockchain(),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                        "--signers", "$ADDITIONAL_PUBKEY_1,$ADDITIONAL_PUBKEY_2",
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertSavedTransaction(result, dir, listOf(DEFAULT_PROVIDER01_PUBKEY), listOf(ADDITIONAL_PUBKEY_1, ADDITIONAL_PUBKEY_2))
                    assertThat(api.getDcModel().capturedOps).isEmpty()
                }
    }

    @Test
    fun `save transaction to file with signers file`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        val signers = writeResourceFileToTempDir(dir, "/signers")
        ManagedRestTestApi(dir)
                .addDcEmptyListQueries("get_compressed_configuration_parts")
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testCommand(
                        CommandProposeBlockchain(),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                        "--signers-file", signers.absolutePath.toString(),
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertSavedTransaction(result, dir, listOf(DEFAULT_PROVIDER01_PUBKEY), listOf(ADDITIONAL_PUBKEY_1, ADDITIONAL_PUBKEY_2))
                    assertThat(api.getDcModel().capturedOps).isEmpty()
                }
    }

    @Test
    fun `save transaction to file with signers DC config`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir)
                .addDcEmptyListQueries("get_compressed_configuration_parts")
                .withDCQuery("get_provider_keys_and_threshold",
                        gtv(mapOf("keys" to gtv(listOf(
                                gtv(DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_1.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_2.hexStringToByteArray()),
                        )), "threshold" to gtv(3))))
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testCommand(
                        CommandProposeBlockchain(),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertSavedTransaction(result, dir, listOf(DEFAULT_PROVIDER01_PUBKEY), listOf(ADDITIONAL_PUBKEY_1, ADDITIONAL_PUBKEY_2))
                    assertThat(api.getDcModel().capturedOps).isEmpty()
                }
    }

    @Test
    fun `ambiguous configuration from DC`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir)
                .addDcEmptyListQueries("get_compressed_configuration_parts")
                .withDCQuery("get_provider_keys_and_threshold",
                        gtv(mapOf("keys" to gtv(listOf(
                                gtv(DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_1.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_2.hexStringToByteArray()),
                        )), "threshold" to gtv(2))))
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testCommand(
                        CommandProposeBlockchain(),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertCommandFailureContains(result, "Transaction needs to be signed by 2 keys of [$DEFAULT_PROVIDER01_PUBKEY, $ADDITIONAL_PUBKEY_1, $ADDITIONAL_PUBKEY_2], please specify which keys to use with --signers option")
                    assertThat(api.getDcModel().capturedOps).isEmpty()
                }
    }

    @Timeout(10, unit = TimeUnit.SECONDS)
    @Test
    fun `ambiguous configuration from DC with manual selection success`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir)
                .addDcEmptyListQueries("get_compressed_configuration_parts")
                .withDCQuery("get_provider_keys_and_threshold",
                        gtv(mapOf("keys" to gtv(listOf(
                                gtv(DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_1.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_2.hexStringToByteArray()),
                        )), "threshold" to gtv(2))))
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testInteractiveCommand(
                        CommandProposeBlockchain(),
                        listOf(
                                KeyboardEvent("ArrowDown"),
                                KeyboardEvent("x"),
                                KeyboardEvent("Enter"),
                        ),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertSavedTransaction(result, dir, listOf(DEFAULT_PROVIDER01_PUBKEY), listOf(ADDITIONAL_PUBKEY_2))
                    assertThat(api.getDcModel().capturedOps).isEmpty()
                }
    }

    @Timeout(10, unit = TimeUnit.SECONDS)
    @Test
    fun `ambiguous configuration from DC with manual selection failure`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir)
                .addDcEmptyListQueries("get_compressed_configuration_parts")
                .withDCQuery("get_provider_keys_and_threshold",
                        gtv(mapOf("keys" to gtv(listOf(
                                gtv(DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_1.hexStringToByteArray()),
                                gtv(ADDITIONAL_PUBKEY_2.hexStringToByteArray()),
                        )), "threshold" to gtv(2))))
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testInteractiveCommand(
                        CommandProposeBlockchain(),
                        listOf(
                                KeyboardEvent("Enter"),
                        ),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertCommandFailureContains(result, "You need to select 1 keys, only 0 was selected")

                    assertThat(api.getDcModel().capturedOps).isEmpty()
                }
    }
}
