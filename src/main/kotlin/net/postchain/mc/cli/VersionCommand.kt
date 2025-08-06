package net.postchain.mc.cli

import com.github.ajalt.clikt.core.PrintMessage

class VersionCommand(private val command: String, private val version: String) :
        PmcCommand("version", "Show the version and exit") {
    override fun run() {
        throw PrintMessage("$command version $version")
    }
}
