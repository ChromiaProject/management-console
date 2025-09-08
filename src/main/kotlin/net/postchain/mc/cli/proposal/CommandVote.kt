package net.postchain.mc.cli.proposal

import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
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
    private val id by proposalIndexOption().required()

    private val vote by option("-y", "--accept", help = "Accept or reject this proposal")
            .flag("-n", "--reject", default = true)

    override fun runDC() {

        transactionBuilder().apply {
                makeVoteOperation(clientProviderPubkey, id, vote)
        }.postAwaitConfirmation(txListener())
                .printResult(
                        "Vote added successfully",
                        "Cannot add vote"
                )
    }
}