package net.postchain.mc.cli.keys

import com.chromia.build.tools.keystore.ChromiaKeyStore
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.common.toHex
import net.postchain.crypto.KeyPair
import net.postchain.crypto.Secp256K1CryptoSystem
import net.postchain.mc.cli.PmcCommand
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

    private val keygenOutputMode: KeygenOutputMode? by mutuallyExclusiveOptions(
            name = "File format",
            option1 = option("-s", "--save", help = "File to save the generated keypair in")
                    .file(canBeDir = false)
                    .convert { KeygenOutputMode.PropertiesFile(it) },

            option2 = option("--key-id", help = "Name the generated key with an id")
                    .convert { KeygenOutputMode.KeyIdFile(it) },
    )
            .single()

    private val nodeFormat by option("-n", "--node", help = "Save the generated keypair in format to be included in node properties file").flag()

    private val printMnemonic by option("-pm", "--print-mnemonic", help = "Print the generated mnemonic").flag()

    /**
     * Cryptographic key generator. Will generate a pair of public and private keys and print to stdout.
     */
    override fun run() {
        if (keygenOutputMode !is KeygenOutputMode.PropertiesFile && nodeFormat) throw UsageError("Cannot use --node without --save")

        val (keyPair, mnemonic) = generateSecp256k1KeyPairWithMnemonic(wordList)

        when (val mode = keygenOutputMode) {
            is KeygenOutputMode.PropertiesFile ->
                saveSecp256k1KeyPair(keyPair, mode.file, nodeFormat)

            is KeygenOutputMode.KeyIdFile -> {
                val name = mode.name
                val chromiaKeyStore = ChromiaKeyStore(name)
                val existingKeyPair = chromiaKeyStore.findKeyPair()
                if (existingKeyPair != null) {
                    throw CliktError("Keypair with id: ${chromiaKeyStore.keyId} already exists")
                }
                chromiaKeyStore.saveKeyPair(keyPair, mnemonic)
            }

            null ->
                echo("privkey:   ${keyPair.privKey.data.toHex()}")
        }

        echo("pubkey:    ${keyPair.pubKey.data.toHex()}")
        if (printMnemonic) {
            echo("mnemonic:  $mnemonic")
        }
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

sealed class KeygenOutputMode {
    data class PropertiesFile(val file: File) : KeygenOutputMode()
    data class KeyIdFile(val name: String) : KeygenOutputMode()
}
