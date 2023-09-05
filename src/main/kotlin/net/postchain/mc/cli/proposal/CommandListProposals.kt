package net.postchain.mc.cli.proposal

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.proposal.ProposalState
import net.postchain.chain0.proposal.ProposalType
import net.postchain.chain0.proposal.getProposalsRange
import net.postchain.chain0.proposal.getRelevantProposals
import net.postchain.chain0.proposal.voting.getProviderVotes
import net.postchain.chain0.version.apiVersion
import net.postchain.common.types.RowId
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.dateToTimestampOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.compatibility.ApiCompatV6.getProposalsSinceV6
import net.postchain.mc.compatibility.ApiCompatV6.getProviderVotesV6
import net.postchain.mc.compatibility.ApiCompatV6.getRelevantProposalsV6


class CommandListProposals : CliktCommand(
        name = "list",
        help = "List all proposals that you can vote on"
) {

    private val config by pmcConfigOption()
    private val client get() = config.client
    private val from by dateToTimestampOption("List proposals from date (YYYY-MM-DD)")
    private val to by dateToTimestampOption("List proposals to date (YYYY-MM-DD)", Long.MAX_VALUE, "9999-12-31", 1)
    private val all by option(help = "Include all proposals, including ones you can not vote on").flag()
    private val pending by option(help = "Only include proposals that are still pending").flag()

    // @Deprecated("Replaced with 'sinceDate' in version 7 of API")
    private val since by option(help = "DEPRECATED: List proposals since proposal id").long().default(0L)

    override fun run() {
        val apiVersion = client.apiVersion()
        when {
            apiVersion >= 7 -> {
                val proposals = if (all) {
                    client.getProposalsRange(from, to, pending).map { ProposalInfo(it.rowid, it.proposalType, it.state) }
                } else {
                    client.getRelevantProposals(from, to, pending, client.pubkey).map { ProposalInfo(it.rowid, it.proposalType, it.state) }
                }
                if (proposals.isEmpty()) return echo("No proposals found")

                val votes = client.getProviderVotes(from, to, client.pubkey)

                echo(defaultTable {
                    header { row("Type", "Id", "State", "Your vote") }
                    body {
                        proposals.forEach { info ->
                            val vote = votes.find { it.proposal == info.rowId }
                            val voteStatus = if (vote == null) "No vote registered" else if (vote.vote) "Accept" else "Reject"
                            row(info.proposalType.toString(), info.rowId.id.toString(), info.state.toString(), voteStatus)
                        }
                    }
                })
            }

            else -> {
                val proposals = if (all) {
                    client.getProposalsSinceV6(RowId(since)).map { it.rowid to it.proposalType }
                } else {
                    client.getRelevantProposalsV6(client.pubkey, RowId(since)).map { it.rowid to it.proposalType }
                }
                if (proposals.isEmpty()) return echo("No proposals found")

                val votes = client.getProviderVotesV6(client.pubkey)

                echo(defaultTable {
                    header { row("Type", "Id", "Your vote") }
                    body {
                        proposals.forEach { (rowid, proposalType) ->
                            val vote = votes.find { it.proposal == rowid }
                            val voteStatus = if (vote == null) "No vote registered" else if (vote.vote) "Accept" else "Reject"
                            row(proposalType.toString(), rowid.id.toString(), voteStatus)
                        }
                    }
                })
            }
        }
    }

    private data class ProposalInfo(val rowId: RowId, val proposalType: ProposalType, val state: ProposalState)
}
