package com.chromia.cli

/** Copied and adapted from https://github.com/ajalt/clikt/blob/master/clikt-mordant/src/commonMain/kotlin/com/github/ajalt/clikt/testing/CliktTesting.kt */

import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.testing.CliktCommandTestResult
import com.github.ajalt.mordant.input.InputEvent
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder


/**
 * Test this command, returning a result that captures the output and result status code.
 *
 * Note that only output printed with [echo][CliktCommand.echo] will be captured. Anything printed
 * with [print] or [println] is not.
 *
 * @param argv The command line to send to the command
 * @param stdin Content of stdin that will be read by prompt options. Multiple inputs should be separated by `\n`.
 * @param inputEvents Input events to pass to an interactive command
 * @param envvars A map of environment variable name to value for envvars that can be read by the command
 * @param includeSystemEnvvars Set to true to include the environment variables from the system in addition to those
 *   defined in [envvars]
 * @param ansiLevel Defaults to no colored output; set to [AnsiLevel.TRUECOLOR] to include ANSI codes in the output.
 * @param width The width of the terminal, used to wrap text
 * @param height The height of the terminal
 * @param hyperlinks Whether to enable hyperlink support in the terminal
 * @param outputInteractive Whether the output is interactive
 * @param inputInteractive Whether the input is interactive
 * @param parse The function to call to parse the command line and run the command
 */
inline fun <T : BaseCliktCommand<T>> BaseCliktCommand<T>.test(
    argv: List<String>,
    stdin: String = "",
    inputEvents: List<InputEvent> = listOf(),
    envvars: Map<String, String> = emptyMap(),
    includeSystemEnvvars: Boolean = false,
    ansiLevel: AnsiLevel = AnsiLevel.NONE,
    width: Int = 79,
    height: Int = 24,
    hyperlinks: Boolean = ansiLevel != AnsiLevel.NONE,
    outputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
    inputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
    parse: (argv: List<String>) -> Unit,
): CliktCommandTestResult {
    var exitCode = 0
    val recorder = TerminalRecorder(
        ansiLevel, width, height, hyperlinks, outputInteractive, inputInteractive
    )
    recorder.inputLines = stdin.split("\n").toMutableList()
    recorder.inputEvents = inputEvents.toMutableList()
    configureContext {
        val originalReader = readEnvvar
        readEnvvar = { envvars[it] ?: (if (includeSystemEnvvars) originalReader(it) else null) }
        terminal = Terminal(
            theme = terminal.theme,
            tabWidth = terminal.tabWidth,
            terminalInterface = recorder
        )
    }
    try {
        parse(argv)
    } catch (e: CliktError) {
        echoFormattedHelp(e)
        exitCode = e.statusCode
    }
    return CliktCommandTestResult(recorder.stdout(), recorder.stderr(), recorder.output(), exitCode)
}
