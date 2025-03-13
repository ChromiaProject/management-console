package net.postchain.mc.cli.economy.proposal

import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.client.core.PostchainClient
import net.postchain.common.types.RowId
import net.postchain.crypto.PubKey
import net.postchain.economy.common_proposal.makeCommonVoteOperation
import net.postchain.economy.common_proposal.makeCommonVoteV65Operation
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.economy.ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION
import net.postchain.mc.cli.economy.ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION
import net.postchain.mc.cli.proposal.util.proposalIndexOption
import net.postchain.mc.compatibility.ApiCompatECV21.makeVoteOperationECV20

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

        when {
            ecVersion.version < ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION -> {
                economyChainClient.transactionBuilder()
                        .makeVoteOperationECV20(clientProviderPubkey, RowId(id), vote)
                        .postAwaitConfirmation()
                        .printResult(
                                "Vote added successfully",
                                "Cannot add vote"
                        )
            }
            ecVersion.version < ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION -> {
                economyChainClient.transactionBuilder()
                        .makeCommonVoteOperation(PubKey(clientProviderPubkey), RowId(id), vote)
                        .postAwaitConfirmation()
                        .printResult(
                                "Vote added successfully",
                                "Cannot add vote"
                        )
            }
            else -> {
                economyChainClient.transactionBuilder()
                        .makeCommonVoteV65Operation(RowId(id), vote)
                        .postAwaitConfirmation()
                        .printResult(
                                "Vote added successfully",
                                "Cannot add vote"
                        )
            }
        }
    }
}