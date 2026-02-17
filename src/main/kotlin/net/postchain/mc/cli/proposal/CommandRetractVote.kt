package net.postchain.mc.cli.proposal

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal.voting.retractVoteOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.proposal.util.proposalIndexOption

class CommandRetractVote : DCBaseCommand(
        name = "retract-vote",
        help = """
            Use this command to retract a previously casted vote on a proposal.
        """.trimIndent()
) {
    private val id by proposalIndexOption().required()

    override fun runDC() {
        transactionBuilder().apply {
            retractVoteOperation(clientProviderPubkey, id)
        }
                .postOrSave()
                .printResult(
                        "Vote retracted successfully",
                        "Cannot retract vote"
                )
    }
}
