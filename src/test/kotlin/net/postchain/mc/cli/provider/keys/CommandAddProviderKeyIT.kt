package net.postchain.mc.cli.provider.keys

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isTrue
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PRIVKEY_1
import net.postchain.mc.cli.test_helpers.ADDITIONAL_PUBKEY_1
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import net.postchain.mc.cli.test_helpers.assertSavedTransaction
import net.postchain.mc.cli.test_helpers.writeToTempDir
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class CommandAddProviderKeyIT {

    @Test
    fun `successful send transaction`(@TempDir dir: Path) {
        val extraSecret = writeToTempDir(dir, "extra_secret", "pubkey=$ADDITIONAL_PUBKEY_1\nprivkey=$ADDITIONAL_PRIVKEY_1\n")
        ManagedRestTestApi(dir)
                .testCommand(
                        CommandAddProviderKey(),
                        "--pubkey", ADDITIONAL_PUBKEY_1,
                        "--secret", dir.resolve(".chromia/config").toAbsolutePath().toString(),
                        "--secret", extraSecret.absolutePath.toString(),
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertCommandSuccessContains(result, "Key $ADDITIONAL_PUBKEY_1 added as provider key")

                    assertThat(api.getDcModel().lastTransaction!!.gtxBody.signers.map { it.toHex() }).containsExactlyInAnyOrder(
                            DEFAULT_PROVIDER01_PUBKEY,
                            ADDITIONAL_PUBKEY_1,
                    )
                    assertThat(api.getDcModel().opWasCalled("add_provider_key") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asInteger() == 0L &&
                                it[2].asByteArray().contentEquals(ADDITIONAL_PUBKEY_1.hexStringToByteArray())
                    }).isTrue()
                }
    }

    @Test
    fun `successful save transaction`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .testCommand(
                        CommandAddProviderKey(),
                        "--pubkey", ADDITIONAL_PUBKEY_1,
                        "--target", dir.absolutePathString(),
                ) { result, _ ->
                    assertSavedTransaction(result, dir, listOf(DEFAULT_PROVIDER01_PUBKEY), listOf(ADDITIONAL_PUBKEY_1))
                }
    }
}
