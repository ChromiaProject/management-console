package net.postchain.mc.cli.keys

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.default
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.common.toHex
import net.postchain.crypto.KeyPair
import net.postchain.crypto.PrivKey
import net.postchain.crypto.PubKey
import net.postchain.crypto.Secp256K1CryptoSystem
import net.postchain.crypto.pqc.dilithium.DilithiumCryptoSystem
import net.postchain.crypto.secp256k1_derivePubKey
import net.postchain.mc.cli.keys.CryptoSystemType.DILITHIUM
import net.postchain.mc.cli.keys.CryptoSystemType.ECDSA
import org.bitcoinj.crypto.MnemonicCode
import java.io.File
import java.io.FileOutputStream
import java.util.Properties

enum class CryptoSystemType(val option: String) {
    ECDSA("--ecdsa"),
    DILITHIUM("--dilithium")
}

class CommandKeygen : CliktCommand(name = "keygen", help = "Generates public/private key pair") {

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

    private val cs by mutuallyExclusiveOptions(
            option(ECDSA.option, help = "ECDSA keys").flag().convert { ECDSA },
            option(DILITHIUM.option, help = "Dilithium keys").flag().convert { DILITHIUM },
            name = "Provider tier",
    ).default(ECDSA)

    /**
     * Cryptographic key generator. Will generate a pair of public and private keys and print to stdout.
     */
    override fun run() {
        if (cs == ECDSA) {
            val (keyPair, mnemonic) = generateSecp256k1KeyPairWithMnemonic(wordList)

            file?.let {
                saveKeyPair(keyPair, it, nodeFormat)
            }
            echo(
                    """
            |privkey:   ${keyPair.privKey.data.toHex()}
            |pubkey:    ${keyPair.pubKey.data.toHex()}
            |mnemonic:  $mnemonic 
        """.trimMargin()
            )
        } else if (cs == DILITHIUM) {
            if (file == null) {
                error("--save option must be specified in case of ${DILITHIUM.option}")
            }

            val keyPair = generateDilithiumKeyPair()
            file?.let {
                saveKeyPair(keyPair, it, nodeFormat)
            }
            echo(keyPair.pubKey.data.toHex())
        }
    }
}

private fun generateSecp256k1KeyPairWithMnemonic(wordList: String): Pair<KeyPair, String> {
    val cs = Secp256K1CryptoSystem()

    var privKey = cs.generatePrivKey().data
    val mnemonicInstance = MnemonicCode.INSTANCE
    var mnemonic = mnemonicInstance.toMnemonic(privKey).joinToString(" ")
    if (wordList.isNotEmpty()) {
        val words = wordList.split(" ")
        mnemonicInstance.check(words)
        mnemonic = wordList
        privKey = mnemonicInstance.toEntropy(words)
    }

    val pubKey = secp256k1_derivePubKey(privKey)

    val keyPair = KeyPair(PubKey(pubKey), PrivKey(privKey))
    return keyPair to mnemonic
}

private fun saveKeyPair(keyPair: KeyPair, file: File, nodeFormat: Boolean) {
    if (file.parentFile != null && !file.parentFile.exists()) file.parentFile.mkdirs()
    val prefix = if (nodeFormat) "messaging." else ""
    val properties = Properties()
    properties["${prefix}privkey"] = keyPair.privKey.data.toHex()
    properties["${prefix}pubkey"] = keyPair.pubKey.data.toHex()

    FileOutputStream(file).use { fs ->
        properties.store(fs, "Keypair generated")
        fs.flush()
    }
}

fun generateDilithiumKeyPair(): KeyPair = DilithiumCryptoSystem().generateKeyPair()