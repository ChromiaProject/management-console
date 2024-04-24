package net.postchain.mc.cli.economy.proposal

import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import net.postchain.client.core.PostchainClient
import net.postchain.common.types.RowId
import net.postchain.crypto.PubKey
import net.postchain.economy.common_proposal.CommonProposalState
import net.postchain.economy.common_proposal.CommonProposalType
import net.postchain.economy.common_proposal.getCommonProposalsRange
import net.postchain.economy.common_proposal.getCommonPubkeyVotes
import net.postchain.economy.common_proposal.getRelevantCommonProposals
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.dateToTimestampOption
import net.postchain.mc.cli.economy.ECBaseCommand
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcTable

class CommandListProposals : ECBaseCommand(
        name = "list",
        help = "List all proposals that you can vote on"
) {
    private val from by dateToTimestampOption("List proposals from date (YYYY-MM-DD)")
    private val to by dateToTimestampOption("List proposals to date (YYYY-MM-DD)", Long.MAX_VALUE, "9999-12-31", 1)
    private val all by option(help = "Include all proposals, including ones you can not vote on").flag()
    private val pending by option(help = "Only include proposals that are still pending").flag()
    private val interactive by interactiveOption()

    private val headers = listOf("Type", "Id", "State", "Your vote")

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        val proposals = if (all) {
            economyChainClient.getCommonProposalsRange(from, to, pending).map { ProposalInfo(it.rowid, it.proposalType, it.state) }
        } else {
            economyChainClient.getRelevantCommonProposals(from, to, pending, client.pubkey).map { ProposalInfo(it.rowid, it.proposalType, it.state) }
        }

        val votes = economyChainClient.getCommonPubkeyVotes(from, to, PubKey(client.pubkey))

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
                showECProposalInfo(client, economyChainClient, proposals[it].rowId)
            }
        }
    }

    private data class ProposalInfo(val rowId: RowId, val proposalType: CommonProposalType, val state: CommonProposalState)

}
