package net.postchain.mc.cli.proposal

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal.revokeProposalOperation
import net.postchain.chain0.proposal.revokeProposalV65Operation
import net.postchain.common.types.RowId
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.proposal.util.proposalIndexOption

class CommandRevokeProposal : DCBaseCommand(
        name = "revoke",
        help = "Revoke/remove a proposal submitted by you"
) {
    private val idx by proposalIndexOption().required()

    override fun runDC() {

        client.transactionBuilder().apply {
            when {
                dcVersion < DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION -> {
                    revokeProposalOperation(clientProviderPubkey, RowId(idx))
                }
                else -> {
                    revokeProposalV65Operation(RowId(idx))
                }
            }
        }
                .postAwaitConfirmation()
                .printResult(
                        "Proposal revoked successfully",
                        "Cannot revoke proposal"
                )
    }
}
