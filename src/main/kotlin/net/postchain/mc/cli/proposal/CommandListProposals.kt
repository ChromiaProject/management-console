package net.postchain.mc.cli.proposal

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.mordant.input.interactiveSelectList
import net.postchain.chain0.proposal.ProposalState
import net.postchain.chain0.proposal.ProposalType
import net.postchain.chain0.proposal.getProposalsRange
import net.postchain.chain0.proposal.getRelevantProposals
import net.postchain.chain0.proposal.voting.getProviderVotes
import net.postchain.common.types.RowId
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.dateToTimestampOption
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV6.getProposalsSinceV6
import net.postchain.mc.compatibility.ApiCompatV6.getProviderVotesV6
import net.postchain.mc.compatibility.ApiCompatV6.getRelevantProposalsV6

class CommandListProposals : DCBaseCommand(
        name = "list",
        help = "List all proposals that you can vote on",
        printHelpOnEmptyArgs = false,
) {
    private val from by dateToTimestampOption("List proposals from date (YYYY-MM-DD)")
    private val to by dateToTimestampOption("List proposals to date (YYYY-MM-DD)", Long.MAX_VALUE, "9999-12-31", 1)
    private val all by option(help = "Include all proposals, including ones you can not vote on").flag()
    private val pending by option(help = "Only include proposals that are still pending").flag()
    private val interactive by interactiveOption()

    // @Deprecated("Replaced with 'sinceDate' in version 7 of API")
    private val since by option(help = "DEPRECATED: List proposals since proposal id").long().default(0L)

    private val headers = listOf("Type", "Id", "State", "Your vote")

    override fun runDC() {
        when {
            dcVersion >= 7 -> {
                val proposals = try {
                    if (all) {
                        client.getProposalsRange(from, to, pending).map { ProposalInfo(it.rowid, it.proposalType, it.state) }
                    } else {
                        client.getRelevantProposals(from, to, pending, clientProviderPubkey).map { ProposalInfo(it.rowid, it.proposalType, it.state) }
                    }
                } catch (e: IllegalArgumentException) {
                    if (e.message?.contains("invalid value") == true && e.message?.contains("for enum class") == true) {
                        val unknownType = extractUnknownEnumValue(e.message)
                        echo("ERROR: The directory chain is using a newer proposal type that is not supported by this version of the management console.", err = true)
                        if (unknownType != null) {
                            echo("Unknown proposal type: $unknownType", err = true)
                        }
                        echo("Please upgrade to the latest version of the management console to view all proposals.", err = true)
                        return
                    }
                    throw e
                }

                val votes = client.getProviderVotes(from, to, clientProviderPubkey)

                if (interactive && proposals.isNotEmpty() && proposals.size < terminal.size.height) {
                    terminal.interactiveSelectList(proposals.map {
                        "${it.rowId.id} - ${it.proposalType} (${it.state})"
                    }, "Select proposal")?.let {
                        showProposalInfo(client, RowId(it.split(' ').first().toLong()))
                    }
                } else {
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
            }

            else -> {
                val proposals = if (all) {
                    client.getProposalsSinceV6(RowId(since)).map { it.rowid to it.proposalType }
                } else {
                    client.getRelevantProposalsV6(clientProviderPubkey, RowId(since)).map { it.rowid to it.proposalType }
                }

                val votes = client.getProviderVotesV6(clientProviderPubkey)

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

    private fun extractUnknownEnumValue(message: String?): String? {
        if (message == null) return null
        val regex = """invalid value (\S+) for enum class""".toRegex()
        return regex.find(message)?.groupValues?.getOrNull(1)
    }
}
