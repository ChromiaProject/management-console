package net.postchain.mc.cli.economy.proposal

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.input.interactiveSelectList
import net.postchain.client.core.PostchainClient
import net.postchain.common.types.RowId
import net.postchain.crypto.PubKey
import net.postchain.economy.common_proposal.GetCommonPubkeyVotesResult
import net.postchain.economy.common_proposal.getCommonProposalsRange
import net.postchain.economy.common_proposal.getCommonPubkeyVotes
import net.postchain.economy.common_proposal.getRelevantCommonProposals
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.dateToTimestampOption
import net.postchain.mc.cli.economy.ECBaseCommand
import net.postchain.mc.cli.economy.ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatECV21.getProposalsRangeECV20
import net.postchain.mc.compatibility.ApiCompatECV21.getProviderVotesECV20
import net.postchain.mc.compatibility.ApiCompatECV21.getRelevantProposalsECV20

class CommandListProposals : ECBaseCommand(
        name = "list",
        help = "List all proposals that you can vote on",
        printHelpOnEmptyArgs = false
) {
    private val from by dateToTimestampOption("List proposals from date (YYYY-MM-DD)")
    private val to by dateToTimestampOption("List proposals to date (YYYY-MM-DD)", Long.MAX_VALUE, "9999-12-31", 1)
    private val all by option(help = "Include all proposals, including ones you can not vote on").flag()
    private val pending by option(help = "Only include proposals that are still pending").flag()
    private val interactive by interactiveOption()

    private val headers = listOf("Type", "Id", "State", "Your vote")

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        val proposals = if (all) {
            when {
                ecVersion.version < ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION -> economyChainClient.getProposalsRangeECV20(from, to, pending)
                        .map { ProposalInfo(it.rowid, it.proposalType.name, it.state.name) }
                else -> economyChainClient.getCommonProposalsRange(from, to, pending)
                        .map { ProposalInfo(it.rowid, it.proposalType.name, it.state.name) }
            }
        } else {
            when {
                ecVersion.version < ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION -> economyChainClient.getRelevantProposalsECV20(from, to, pending, client.pubkey)
                        .map { ProposalInfo(it.rowid, it.proposalType.name, it.state.name) }
                else -> economyChainClient.getRelevantCommonProposals(from, to, pending, client.pubkey)
                        .map { ProposalInfo(it.rowid, it.proposalType.name, it.state.name) }
            }
        }

        val votes = when {
            ecVersion.version < ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION -> economyChainClient.getProviderVotesECV20(from, to, client.pubkey)
                    .map { GetCommonPubkeyVotesResult(it.proposal, it.vote) }
            else -> economyChainClient.getCommonPubkeyVotes(from, to, PubKey(client.pubkey))
        }

        if (interactive && proposals.isNotEmpty() && proposals.size < terminal.size.height) {
            terminal.interactiveSelectList(proposals.map {
                "${it.rowId.id} - ${it.proposalType} (${it.state})"
            }, "Select proposal")?.let {
                showECProposalInfo(client, economyChainClient, RowId(it.split(' ').first().toLong()), ecVersion.version)
            }
        } else {
            echo(pmcTable(
                    "proposals",
                    headers,
                    proposals.map { info ->
                        val vote = votes.find { it.proposal == info.rowId }
                        val voteStatus = if (vote == null) "No vote registered" else if (vote.vote) "Accept" else "Reject"
                        listOf(info.proposalType, info.rowId.id.toString(), info.state, voteStatus)
                    },
                    null,
                    interactive
            ))
            if (interactive && proposals.isNotEmpty()) {
                promptForIndex(proposals)?.let {
                    showECProposalInfo(client, economyChainClient, proposals[it].rowId, ecVersion.version)
                }
            }
        }
    }

    private data class ProposalInfo(val rowId: RowId, val proposalType: String, val state: String)

}
