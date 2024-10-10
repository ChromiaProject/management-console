package net.postchain.mc.cli.economy.mint

import com.github.ajalt.clikt.core.subcommands
import net.postchain.mc.cli.PmcCommand

class MintCommand : PmcCommand(help = "Manage voter set and proposals for minting") {
    override fun run() = Unit
}

fun mintCommands() = MintCommand().subcommands(
        CommandVoterSetCreate(),
        CommandVoterSetInfo(),
        CommandVoterSetUpdate(),
        CommandMintPropose(),
)
