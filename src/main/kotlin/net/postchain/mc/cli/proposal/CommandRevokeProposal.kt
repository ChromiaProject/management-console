package net.postchain.mc.cli.proposal

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal.revokeProposalOperation
import net.postchain.common.types.RowId
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.proposal.util.proposalIndexOption
import net.postchain.mc.cli.util.pmcConfigOption


class CommandRevokeProposal : PmcCommand(
        name = "revoke",
        help = "Revoke/remove a proposal submitted by you"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val idx by proposalIndexOption().required()

    override fun run() {
        client.transactionBuilder()
                .revokeProposalOperation(client.pubkey, RowId(idx))
                .postAwaitConfirmation()
                .printResult(
                        "Proposal revoked successfully",
                        "Cannot revoke proposal"
                )
    }
}
