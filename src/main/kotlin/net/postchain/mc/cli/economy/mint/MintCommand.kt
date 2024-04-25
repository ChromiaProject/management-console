package net.postchain.mc.cli.economy.mint

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class MintCommand : CliktCommand("Manage voter set and proposals for minting") {
    override fun run() = Unit
}

fun mintCommands() = MintCommand().subcommands(
        CommandVoterSetCreate(),
        CommandVoterSetInfo(),
        CommandVoterSetUpdate(),
        CommandMintPropose(),
)
