package net.postchain.mc.cli.test_helpers

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.testing.CliktCommandTestResult
import com.github.ajalt.clikt.testing.test
import java.io.File
import java.nio.file.Path


/**
 * Run a command and pass the dynamically created .chromia/config file as parameter.
 */
fun testPmcCommand(dir: Path, command: CliktCommand, vararg args: String): CliktCommandTestResult {
    val parametersWithConfig = args.toMutableList() + listOf(
            "--config", dir.resolve(".chromia/config").toAbsolutePath().toString()
    )
    return command.test(*parametersWithConfig.toTypedArray())
}

fun assertLineValue(content: String, name: String, value: String) {
    assertLineValue(content.lines(), name, value)
}

fun assertLineValue(lines: List<String>, name: String, value: String) {
    assertThat(lines
            .filter { it.contains("\"$name\":") }
            .any { it.contains(value) }
    ).isTrue()
}

fun assertCommandOutput(output: String, expected: String) {
    assertThat(normalizeCommandOutput(output)).isEqualTo(normalizeCommandOutput(expected))
}

fun assertCommandContains(output: String, expected: String) {
    assertThat(normalizeCommandOutput(output)).contains(normalizeCommandOutput(expected))
}

fun normalizeCommandOutput(output: String): String {
    return output
            .trim()
            .replace(Regex(" {2,}"), "")
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
