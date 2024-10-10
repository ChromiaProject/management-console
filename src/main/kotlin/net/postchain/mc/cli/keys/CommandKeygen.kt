package net.postchain.mc.cli.keys

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.common.toHex
import net.postchain.crypto.KeyPair
import net.postchain.crypto.Secp256K1CryptoSystem
import java.io.File
import java.io.FileOutputStream
import java.util.Properties

class CommandKeygen : PmcCommand(name = "keygen", help = "Generates public/private key pair") {

    private val wordList by option(
            "-m", "--mnemonic",
            help = """
            Mnemonic word list, words separated by space, e.g:
                "lift employ roast rotate liar holiday sun fever output magnet...""
        """.trimIndent()
    )
            .default("")

    private val file by option("-s", "--save", help = "File to save the generated keypair in")
            .file(canBeDir = false)

    private val nodeFormat by option("-n", "--node", help = "Save the generated keypair in format to be included in node properties file").flag()


    /**
     * Cryptographic key generator. Will generate a pair of public and private keys and print to stdout.
     */
    override fun run() {
        val (keyPair, mnemonic) = generateSecp256k1KeyPairWithMnemonic(wordList)

        file?.let {
            saveSecp256k1KeyPair(keyPair, it, nodeFormat)
        }
        if (file == null) {
            echo("privkey:   ${keyPair.privKey.data.toHex()}")
        }
        echo("pubkey:    ${keyPair.pubKey.data.toHex()}")
        echo("mnemonic:  $mnemonic")
    }
}

private fun generateSecp256k1KeyPairWithMnemonic(wordList: String): Pair<KeyPair, String> {
    val cs = Secp256K1CryptoSystem()

    if (wordList.isNotEmpty()) {
        // New Secp256K1CryptoSystem were the mnemonic is bip39 compatible
        return cs.recoverKeyPairFromMnemonic(wordList)
    }
    return cs.generateKeyPairWithMnemonic()
}

private fun saveSecp256k1KeyPair(keyPair: KeyPair, file: File, nodeFormat: Boolean) {
    if (file.parentFile != null && !file.parentFile.exists()) file.parentFile.mkdirs()
    val prefix = if (nodeFormat) "messaging." else ""
    val properties = Properties()
    properties["${prefix}privkey"] = keyPair.privKey.data.toHex()
    properties["${prefix}pubkey"] = keyPair.pubKey.data.toHex()

    FileOutputStream(file).use { fs ->
        properties.store(fs, "Keypair generated using secp256k1")
        fs.flush()
    }
}
