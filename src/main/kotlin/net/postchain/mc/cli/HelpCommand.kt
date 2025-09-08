package net.postchain.mc.cli

import com.github.ajalt.clikt.core.PrintHelpMessage

class HelpCommand: PmcCommand("help","Show this message and exit") {
    override fun run() {
        throw PrintHelpMessage(null)
    }
}
