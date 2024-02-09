package net.postchain.mc.cli.economy.proposal

import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.client.core.PostchainClient
import net.postchain.common.types.RowId
import net.postchain.economy.economy_chain.ec_proposal.makeVoteOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.economy.ECBaseCommand
import net.postchain.mc.cli.proposal.util.proposalIndexOption

class CommandVote : ECBaseCommand(
        name = "vote",
        help = """
            Providers decide if proposed changes should be applied. Use this function to vote yes or no to a proposal.
        """.trimIndent()
) {
    private val id by proposalIndexOption().required()

    private val vote by option("-y", "--accept", help = "Accept or reject this proposal")
            .flag("-n", "--reject", default = true)

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        economyChainClient.transactionBuilder()
                .makeVoteOperation(client.pubkey, RowId(id), vote)
                .postAwaitConfirmation()
                .printResult(
                        "Vote added successfully",
                        "Cannot add vote"
                )
    }
}