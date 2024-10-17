package net.postchain.mc.cli.proposal

import net.postchain.mc.cli.PmcCommand
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
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV6.getProposalsSinceV6
import net.postchain.mc.compatibility.ApiCompatV6.getProviderVotesV6
import net.postchain.mc.compatibility.ApiCompatV6.getRelevantProposalsV6

class CommandListProposals : PmcCommand(
        name = "list",
        help = "List all proposals that you can vote on",
) {

    private val config by pmcConfigOption()
    private val client get() = config.client
    private val from by dateToTimestampOption("List proposals from date (YYYY-MM-DD)")
    private val to by dateToTimestampOption("List proposals to date (YYYY-MM-DD)", Long.MAX_VALUE, "9999-12-31", 1)
    private val all by option(help = "Include all proposals, including ones you can not vote on").flag()
    private val pending by option(help = "Only include proposals that are still pending").flag()
    private val interactive by interactiveOption()

    // @Deprecated("Replaced with 'sinceDate' in version 7 of API")
    private val since by option(help = "DEPRECATED: List proposals since proposal id").long().default(0L)

    private val headers = listOf("Type", "Id", "State", "Your vote")

    override fun run() {
        val apiVersion = client.apiVersion()
        when {
            apiVersion >= 7 -> {
                val proposals = if (all) {
                    client.getProposalsRange(from, to, pending).map { ProposalInfo(it.rowid, it.proposalType, it.state) }
                } else {
                    client.getRelevantProposals(from, to, pending, client.pubkey).map { ProposalInfo(it.rowid, it.proposalType, it.state) }
                }

                val votes = client.getProviderVotes(from, to, client.pubkey)

                echo(pmcTable(
                        "proposals",
                        headers,
                        proposals.map { info ->
                            val vote = votes.find { it.proposal == info.rowId }
                            val voteStatus = if (vote == null) "No vote registered" else if (vote.vote) "Accept" else "Reject"
                            listOf(info.proposalType.toString(), info.rowId.id.toString(), info.state.toString(), voteStatus)
                        },
                        null,
                        interactive
                ))
                if (interactive && proposals.isNotEmpty()) {
                    promptForIndex(proposals)?.let {
                        showProposalInfo(client, proposals[it].rowId)
                    }
                }
            }

            else -> {
                val proposals = if (all) {
                    client.getProposalsSinceV6(RowId(since)).map { it.rowid to it.proposalType }
                } else {
                    client.getRelevantProposalsV6(client.pubkey, RowId(since)).map { it.rowid to it.proposalType }
                }

                val votes = client.getProviderVotesV6(client.pubkey)

                echo(pmcTable(
                        "proposals",
                        listOf("Type", "Id", "Your vote"),
                        proposals.map { (rowid, proposalType) ->
                            val vote = votes.find { it.proposal == rowid }
                            val voteStatus = if (vote == null) "No vote registered" else if (vote.vote) "Accept" else "Reject"
                            listOf(proposalType.toString(), rowid.id.toString(), voteStatus)
                        }
                ))
            }
        }
    }

    private data class ProposalInfo(val rowId: RowId, val proposalType: ProposalType, val state: ProposalState)
}
