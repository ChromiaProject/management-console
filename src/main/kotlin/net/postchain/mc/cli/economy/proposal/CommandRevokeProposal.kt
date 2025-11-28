package net.postchain.mc.cli.economy.proposal

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.client.core.PostchainClient
import net.postchain.common.types.RowId
import net.postchain.economy.common_proposal.revokeCommonProposalOperation
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.proposal.util.proposalIndexOption

class CommandRevokeProposal : ECBaseCommand(
        name = "revoke",
        help = "Revoke/remove a proposal submitted by you"
) {
    private val idx by proposalIndexOption().required()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        transactionBuilder()
                .revokeCommonProposalOperation(clientProviderPubkey, RowId(idx))
                .postOrSave()
                .printResult(
                        "Proposal revoked successfully",
                        "Cannot revoke proposal"
                )
    }
}
