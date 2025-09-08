package net.postchain.mc.cli.keys

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEmpty
import com.github.ajalt.clikt.testing.test
import net.postchain.common.PropertiesFileLoader
import net.postchain.crypto.Secp256K1CryptoSystem
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import java.nio.file.Path
import java.util.concurrent.Callable
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

class KeygenTest {

    // mostly test external lib
    @Test
    fun testMnemonic() {
        val cs = Secp256K1CryptoSystem()
        val (keyPair, mnemonic) = cs.generateKeyPairWithMnemonic()
        val (recoveredKeypair, _) = cs.recoverKeyPairFromMnemonic(mnemonic)
        assertThat(keyPair).isEqualTo(recoveredKeypair)
    }

    @Test
    fun keygen() {
        val file = createTempFile()
        CommandKeygen().test(arrayOf(
                "-m", "picnic shove leader great protect table leg witness walk night cable caution about produce engage armor first burden olive violin cube gentle bulk train",
                "-s", file.absolutePathString()))

        val keys = PropertiesFileLoader.load(file.absolutePathString())
        assertThat(keys.getString("pubkey")).isEqualTo("02AF635148608B9A18DF11241F1862624C3E7CCEDEC0864FEE00B3D4E7093CC4CF")
        assertThat(keys.getString("privkey")).isEqualTo("CE59E2F0E7342EFB12A889B4168E3C7D909EC858849C0CA5FFAB78541C22AB65")

        assertFailure {
            CommandKeygen().test(arrayOf("-m", "invalid mnemonic"))
        }.isInstanceOf(IllegalArgumentException::class).hasMessage("Invalid number of words in mnemonic. Supported number of words are 12 or 24")
    }

    @Test
    fun `keygen with node option should save with prefix`() {
        val file = createTempFile()
        CommandKeygen().test(arrayOf(
                "-m", "picnic shove leader great protect table leg witness walk night cable caution about produce engage armor first burden olive violin cube gentle bulk train",
                "-s", file.absolutePathString(),
                "-n"))

        val keys = PropertiesFileLoader.load(file.absolutePathString())
        assertThat(keys.getString("messaging.pubkey")).isEqualTo("02AF635148608B9A18DF11241F1862624C3E7CCEDEC0864FEE00B3D4E7093CC4CF")
        assertThat(keys.getString("messaging.privkey")).isEqualTo("CE59E2F0E7342EFB12A889B4168E3C7D909EC858849C0CA5FFAB78541C22AB65")
    }

    @Test
    fun invalidLengthOfMnemonic() {
        assertFailure {
            CommandKeygen().test(arrayOf("-m", "invalid mnemonic"))
        }.isInstanceOf(IllegalArgumentException::class).hasMessage("Invalid number of words in mnemonic. Supported number of words are 12 or 24")
    }

    @Test
    fun storeKeyPairFilesWithUserSetKeyId(@TempDir dir: Path) {
        val myKeyId = "myKeyId"
        val result = EnvironmentVariables("CHROMIA_HOME", dir.toString()).execute(Callable {
            CommandKeygen().test(arrayOf("--key-id", myKeyId))
        })
        assertThat(result.statusCode).isEqualTo(0)
        val out = result.stdout
        assertThat(out).contains("pubkey:")
        val privateKeyFile = dir.resolve(myKeyId)
        val publicKeyFile = dir.resolve("$myKeyId.pubkey")
        val mnemonicFile = dir.resolve("${myKeyId}_mnemonic")

        dir.listDirectoryEntries().containsAll(listOf(privateKeyFile, publicKeyFile, mnemonicFile))
        assertThat(privateKeyFile.readText()).isNotEmpty()
        assertThat(publicKeyFile.readText()).isNotEmpty()
        assertThat(mnemonicFile.readText()).contains("Mnemonic phrase generated:")
    }
}
