package net.postchain.mc.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import net.postchain.common.PropertiesFileLoader
import net.postchain.common.toHex
import net.postchain.crypto.Secp256K1CryptoSystem
import net.postchain.mc.cli.keys.CommandKeygen
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.io.path.absolutePathString
import org.junit.jupiter.api.Assertions.assertEquals

class KeygenTest {

    // mostly test external lib
    @Test
    fun testMnemonic() {
        val cs = Secp256K1CryptoSystem()
        val (keyPair, mnemonic) = cs.generateKeyPairWithMnemonic()
        val (recoverdKeypair, _) = cs.recoverKeyPairFromMnemonic(mnemonic)
        assertEquals(keyPair, recoverdKeypair)
    }
    
    @Test
    fun keygen() {
        val file = kotlin.io.path.createTempFile()
        CommandKeygen().parse(arrayOf(
                "-m", "picnic shove leader great protect table leg witness walk night cable caution about produce engage armor first burden olive violin cube gentle bulk train",
                "-s", file.absolutePathString()))

        val keys = PropertiesFileLoader.load(file.absolutePathString())
        assertThat(keys.getString("pubkey")).isEqualTo("02AF635148608B9A18DF11241F1862624C3E7CCEDEC0864FEE00B3D4E7093CC4CF")
        assertThat(keys.getString("privkey")).isEqualTo("CE59E2F0E7342EFB12A889B4168E3C7D909EC858849C0CA5FFAB78541C22AB65")

        val exception = assertThrows<IllegalArgumentException> {
            CommandKeygen().parse(arrayOf("-m", "invalid mnemonic"))
        }
        assertThat(exception.message).isEqualTo("Invalid number of words in mnemonic. Supported number of words are 12 or 24")
    }

    @Test
    fun `keygen with node option should save with prefix`() {
        val file = kotlin.io.path.createTempFile()
        CommandKeygen().parse(arrayOf(
                "-m", "picnic shove leader great protect table leg witness walk night cable caution about produce engage armor first burden olive violin cube gentle bulk train",
                "-s", file.absolutePathString(),
                "-n"))

        val keys = PropertiesFileLoader.load(file.absolutePathString())
        assertThat(keys.getString("messaging.pubkey")).isEqualTo("02AF635148608B9A18DF11241F1862624C3E7CCEDEC0864FEE00B3D4E7093CC4CF")
        assertThat(keys.getString("messaging.privkey")).isEqualTo("CE59E2F0E7342EFB12A889B4168E3C7D909EC858849C0CA5FFAB78541C22AB65")
    }


    @Test
    fun invalidLengthOfMnemonic() {
        val exception = assertThrows<IllegalArgumentException> {
            CommandKeygen().parse(arrayOf("-m", "invalid mnemonic"))
        }
        assertEquals("Invalid number of words in mnemonic. Supported number of words are 12 or 24", exception.message)
    }
}