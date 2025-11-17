package net.postchain.mc.cli.economy.proposal

import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import net.postchain.client.core.PostchainClient
import net.postchain.common.types.RowId
import net.postchain.crypto.PubKey
import net.postchain.economy.common_proposal.makeCommonVoteOperation
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.proposal.util.proposalIndexOption
import net.postchain.mc.compatibility.ApiCompatECV21.makeVoteOperationECV20
import net.postchain.mc.compatibility.ApiCompatECV57.makeCommonVoteOperation
import net.postchain.mc.compatibility.ApiCompatECV57.makeCommonVoteV65Operation

class CommandVote : ECBaseCommand(
        name = "vote",
        help = """
            Providers decide if proposed changes should be applied. Use this function to vote yes or no to a proposal.
        """.trimIndent()
) {
    private val id by proposalIndexOption()

    private val ids by option("--ids", help = "Comma-separated list of proposal IDs to vote on")
            .convert { it.toLong() }
            .split(",")

    private val vote by option("-y", "--accept", help = "Accept or reject this proposal")
            .flag("-n", "--reject", default = true)

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        // Validate that exactly one of --id or --ids is provided
        val proposalIds = when {
            id != null && ids != null -> throw UsageError("Cannot specify both --id and --ids")
            id != null -> listOf(id!!)
            ids != null -> ids!!
            else -> throw UsageError("Either --id or --ids must be specified")
        }

        when {
            ecVersion.version < ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION -> {
                transactionBuilder().apply {
                    proposalIds.forEach { proposalId ->
                        makeVoteOperationECV20(clientProviderPubkey, RowId(proposalId), vote)
                    }
                }.postOrSave()
                        .printResult(
                                if (proposalIds.size == 1) "Vote added successfully"
                                else "Vote added successfully for ${proposalIds.size} proposals",
                                "Cannot add vote"
                        )
            }
            ecVersion.version < ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION -> {
                transactionBuilder().apply {
                    proposalIds.forEach { proposalId ->
                        makeCommonVoteOperation(PubKey(clientProviderPubkey), RowId(proposalId), vote)
                    }
                }.postOrSave()
                        .printResult(
                                if (proposalIds.size == 1) "Vote added successfully"
                                else "Vote added successfully for ${proposalIds.size} proposals",
                                "Cannot add vote"
                        )
            }
            ecVersion.version < ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION -> {
                transactionBuilder().apply {
                    proposalIds.forEach { proposalId ->
                        makeCommonVoteV65Operation(RowId(proposalId), vote)
                    }
                }.postOrSave()
                        .printResult(
                                if (proposalIds.size == 1) "Vote added successfully"
                                else "Vote added successfully for ${proposalIds.size} proposals",
                                "Cannot add vote"
                        )
            }
            else -> {
                transactionBuilder().apply {
                    proposalIds.forEach { proposalId ->
                        makeCommonVoteOperation(clientProviderPubkey, RowId(proposalId), vote)
                    }
                }.postOrSave()
                        .printResult(
                                if (proposalIds.size == 1) "Vote added successfully"
                                else "Vote added successfully for ${proposalIds.size} proposals",
                                "Cannot add vote"
                        )
            }
        }
    }
}