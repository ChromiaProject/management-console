package net.postchain.mc.cli.economy.proposal

import com.github.ajalt.clikt.core.subcommands
import net.postchain.mc.cli.PmcCommand

class ProposalCommand : PmcCommand(help = "Interact with existing proposals") {
    override fun run() = Unit
}

fun proposalCommands() = ProposalCommand().subcommands(
    CommandGetProposal(),
    CommandListProposals(),
    CommandRevokeProposal(),
    CommandVote(),
)
