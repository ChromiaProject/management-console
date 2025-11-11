package net.postchain.mc.cli.proposal

import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import net.postchain.chain0.proposal.voting.makeVoteOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.proposal.util.proposalIndexOption

class CommandVote : DCBaseCommand(
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

    override fun runDC() {
        // Validate that exactly one of --id or --ids is provided
        val proposalIds = when {
            id != null && ids != null -> throw UsageError("Cannot specify both --id and --ids")
            id != null -> listOf(id!!)
            ids != null -> ids!!
            else -> throw UsageError("Either --id or --ids must be specified")
        }

        transactionBuilder().apply {
            proposalIds.forEach { proposalId ->
                makeVoteOperation(clientProviderPubkey, proposalId, vote)
            }
        }.postOrSave()
                .printResult(
                        if (proposalIds.size == 1) "Vote added successfully"
                        else "Vote added successfully for ${proposalIds.size} proposals",
                        "Cannot add vote"
                )



    }
}