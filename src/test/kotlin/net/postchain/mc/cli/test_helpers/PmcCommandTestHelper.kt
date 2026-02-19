package net.postchain.mc.cli.test_helpers

import assertk.assertThat
import assertk.assertions.any
import assertk.assertions.contains
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import com.chromia.build.tools.config.SUPPRESS_KEY_STORAGE_DEPRECATION_WARNING_SYSTEM_PROPERTY
import com.chromia.build.tools.multisignature.MultiSignatureTxData
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.clikt.testing.CliktCommandTestResult
import com.github.ajalt.clikt.testing.test
import com.github.ajalt.mordant.input.InputEvent
import net.postchain.common.BlockchainRid
import net.postchain.common.toHex
import net.postchain.gtx.Gtx
import org.opentest4j.AssertionFailedError
import java.io.File
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

/**
 * Run a command and pass the dynamically created .chromia/config file as parameter.
 */
fun testPmcCommand(dir: Path, command: CliktCommand, vararg args: String): CliktCommandTestResult {
    System.setProperty(SUPPRESS_KEY_STORAGE_DEPRECATION_WARNING_SYSTEM_PROPERTY, "true")
    val parametersWithConfig = args.toMutableList() + listOf(
            "--config", dir.resolve(".chromia/config").toAbsolutePath().toString()
    )
    return command.test(*parametersWithConfig.toTypedArray())
}

/**
 * Run an interactive command and pass the dynamically created .chromia/config file as parameter.
 */
fun testInteractivePmcCommand(dir: Path, command: CliktCommand, inputEvents: List<InputEvent> = listOf(), vararg args: String): CliktCommandTestResult {
    System.setProperty(SUPPRESS_KEY_STORAGE_DEPRECATION_WARNING_SYSTEM_PROPERTY, "true")
    val parametersWithConfig = args.toMutableList() + listOf(
            "--config", dir.resolve(".chromia/config").toAbsolutePath().toString()
    )
    return command.test(inputEvents = inputEvents, argv = parametersWithConfig, inputInteractive = true, outputInteractive = true) { command.parse(it) }
}

/**
 * Run an interactive command with stdin input and pass the dynamically created .chromia/config file as parameter.
 */
fun testInteractivePmcCommand(dir: Path, command: CliktCommand, stdin: String, vararg args: String): CliktCommandTestResult {
    System.setProperty(SUPPRESS_KEY_STORAGE_DEPRECATION_WARNING_SYSTEM_PROPERTY, "true")
    val parametersWithConfig = args.toMutableList() + listOf(
            "--config", dir.resolve(".chromia/config").toAbsolutePath().toString()
    )
    return command.test(stdin = stdin, argv = parametersWithConfig, inputInteractive = true, outputInteractive = true) { command.parse(it) }
}

fun assertLineValue(content: String, name: String, value: String) {
    assertLineValue(content.lines(), name, value)
}

fun assertLineValue(lines: List<String>, name: String, value: String) {
    assertThat(lines
            .filter { it.contains("\"$name\":") },
            name).any { it.contains(value) }
}

fun assertCommandSuccess(result: CliktCommandTestResult) {
    if (result.statusCode != 0) {
        assertThat(result.stderr).isEqualTo("")
    }
}

fun assertCommandOutput(output: String, expected: String) {
    assertThat(normalizeCommandOutput(output)).isEqualTo(normalizeCommandOutput(expected))
}

fun assertCommandOutputContains(output: String, expected: String) {
    assertThat(normalizeCommandOutput(output)).contains(normalizeCommandOutput(expected))
}

fun assertCommandSuccessContains(result: CliktCommandTestResult, expected: String) {
    assertCommandOutputContains(result.output, expected)
    assertThat(result.statusCode).isEqualTo(0)
}

fun assertCommandFailureContains(result: CliktCommandTestResult, errorMessage: String) {
    assertCommandOutputContains(result.stderr, errorMessage)
    assertThat(result.statusCode).isGreaterThan(0)
}

fun assertSavedTransaction(result: CliktCommandTestResult, dir: Path, blockchainRid: BlockchainRid, initialSigners: List<String>, additionalSigners: List<String>) {
    assertCommandSuccessContains(result, "is written as hex to file: ")
    if (additionalSigners.isNotEmpty()) {
        assertCommandSuccessContains(result, "Requires additional signatures by: $additionalSigners")
    } else {
        assertCommandSuccessContains(result, "Is fully signed and ready to be sent")
    }
    val transactionFiles = dir.listDirectoryEntries("transaction_*")
    if (transactionFiles.size != 1) throw AssertionFailedError(result.output)
    val savedTransactionData = transactionFiles.single().readText()
    val savedTransaction = MultiSignatureTxData.decode(savedTransactionData)
    val gtx = Gtx.decode(savedTransaction.transaction)
    assertThat(gtx.gtxBody.blockchainRid).isEqualTo(blockchainRid)
    assertThat(gtx.gtxBody.signers.map { it.toHex() }).containsExactlyInAnyOrder(*(initialSigners + additionalSigners).toTypedArray())
    assertThat(gtx.signatures.filterNot { it.isEmpty() }).hasSize(initialSigners.size)
}

fun normalizeCommandOutput(output: String): String {
    return output
            .trim()
            .replace(Regex(" {2,}"), "")
}

fun writeToTempDir(dir: Path, name: String, content: String): File {
    val file = File(dir.toFile(), name)
    with(file) {
        parentFile.mkdirs()
        file.writeText(content)
    }
    return file
}

fun writeResourceFileToTempDir(dir: Path, resource: String): File {
    val file = File(dir.toFile(), File(resource).name)
    with(file) {
        parentFile.mkdirs();
        {}.javaClass.getResourceAsStream(resource)?.use { input ->
            outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
    return file
}

fun writeChromiaConfig(dir: Path, apiUrl: String, providerPubKey: String?, keyId: String?, pubKey: String, privKey: String, dcBcRid: BlockchainRid): File {
    val file = File(dir.toFile(), ".chromia/config")
    with(file) {
        parentFile.mkdirs()
        writeText("""
                    api.url = $apiUrl
                    ${if (providerPubKey != null) "provider.pubkey=$providerPubKey" else ""}
                    ${if (keyId != null) "key.id=$keyId" else ""}
                    pubkey=$pubKey
                    privkey=$privKey
                    brid=$dcBcRid
                    """.trimIndent())
    }
    return file
}
