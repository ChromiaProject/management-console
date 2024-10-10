package net.postchain.mc.cli.votingupdates

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands

class VoterSetCommand : NoOpCliktCommand(name = "voterset") {
    override fun help(context: Context) = "Voter set commands"
}

fun voterSetCommands() = VoterSetCommand().subcommands(
    CommandProposeVoterSetUpdate(),
    CommandCreateVoterSet(),
    CommandListVoterSets(),
    CommandVoterSetInfo(),
)
