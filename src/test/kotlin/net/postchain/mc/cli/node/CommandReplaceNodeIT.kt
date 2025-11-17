package net.postchain.mc.cli.node

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isTrue
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PRIVKEY_1
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PRIVKEY_2
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PUBKEY_1
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PUBKEY_2
import net.postchain.mc.cli.test_helpers.DEFAULT_BRID_DIRECTORY_CHAIN
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import net.postchain.mc.cli.test_helpers.assertSavedTransaction
import net.postchain.mc.cli.test_helpers.writeToTempDir
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class CommandReplaceNodeIT {

    @Test
    fun `successful send transaction`(@TempDir dir: Path) {
        val extraSecret1 = writeToTempDir(dir, "extra_secret1", "pubkey=$ADDITIONAL_PUBKEY_1\nprivkey=$ADDITIONAL_PRIVKEY_1\n")
        val extraSecret2 = writeToTempDir(dir, "extra_secret2", "pubkey=$ADDITIONAL_PUBKEY_2\nprivkey=$ADDITIONAL_PRIVKEY_2\n")
        ManagedRestTestApi(dir)
                .testCommand(
                        CommandReplaceNode(),
                        "--old-key", ADDITIONAL_PUBKEY_1,
                        "--new-key", ADDITIONAL_PUBKEY_2,
                        "--secret", dir.resolve(".chromia/config").toAbsolutePath().toString(),
                        "--secret", extraSecret1.absolutePath.toString(),
                        "--secret", extraSecret2.absolutePath.toString(),
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertCommandSuccessContains(result, "Node has been replaced")

                    assertThat(api.getDcModel().lastTransaction!!.gtxBody.signers.map { it.toHex() }).containsExactlyInAnyOrder(
                            DEFAULT_PROVIDER01_PUBKEY,
                            ADDITIONAL_PUBKEY_1,
                            ADDITIONAL_PUBKEY_2,
                    )
                    assertThat(api.getDcModel().opWasCalled("replace_node_with_node_data") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray())
                    }).isTrue()
                }
    }

    @Test
    fun `successful save transaction`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .testCommand(
                        CommandReplaceNode(),
                        "--old-key", ADDITIONAL_PUBKEY_1,
                        "--new-key", ADDITIONAL_PUBKEY_2,
                        "--target", dir.absolutePathString(),
                ) { result, _ ->
                    assertSavedTransaction(result, dir, DEFAULT_BRID_DIRECTORY_CHAIN, listOf(DEFAULT_PROVIDER01_PUBKEY), listOf(ADDITIONAL_PUBKEY_1, ADDITIONAL_PUBKEY_2))
                }
    }
}
