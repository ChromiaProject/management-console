package net.postchain.mc.cli.economy.proposal

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.client.core.PostchainClient
import net.postchain.common.types.RowId
import net.postchain.crypto.PubKey
import net.postchain.economy.common_proposal.revokeCommonProposalOperation
import net.postchain.economy.common_proposal.revokeCommonProposalV65Operation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.economy.ECBaseCommand
import net.postchain.mc.cli.economy.ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION
import net.postchain.mc.cli.economy.ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION
import net.postchain.mc.cli.proposal.util.proposalIndexOption
import net.postchain.mc.compatibility.ApiCompatECV21.revokeProposalOperationECV20

class CommandRevokeProposal : ECBaseCommand(
        name = "revoke",
        help = "Revoke/remove a proposal submitted by you"
) {
    private val idx by proposalIndexOption().required()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        when {
            ecVersion.version < ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION -> {
                economyChainClient.transactionBuilder()
                        .revokeProposalOperationECV20(client.pubkey, RowId(idx))
                        .postAwaitConfirmation()
                        .printResult(
                                "Proposal revoked successfully",
                                "Cannot revoke proposal"
                        )
            }
            ecVersion.version < ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION -> {
                economyChainClient.transactionBuilder()
                        .revokeCommonProposalOperation(PubKey(client.pubkey), RowId(idx))
                        .postAwaitConfirmation()
                        .printResult(
                                "Proposal revoked successfully",
                                "Cannot revoke proposal"
                        )
            }
            else -> {
                economyChainClient.transactionBuilder()
                        .revokeCommonProposalV65Operation(RowId(idx))
                        .postAwaitConfirmation()
                        .printResult(
                                "Proposal revoked successfully",
                                "Cannot revoke proposal"
                        )
            }
        }
    }
}
