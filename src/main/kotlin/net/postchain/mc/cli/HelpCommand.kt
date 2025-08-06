package net.postchain.mc.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.PrintHelpMessage

class HelpCommand: CliktCommand("help") {
    override fun help(context: Context): String = "Show this message and exit"

    override fun run() {
        throw PrintHelpMessage(null)
    }
}
