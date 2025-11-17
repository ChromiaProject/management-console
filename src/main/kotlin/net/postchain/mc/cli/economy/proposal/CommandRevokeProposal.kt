package net.postchain.mc.cli.economy.proposal

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.client.core.PostchainClient
import net.postchain.common.types.RowId
import net.postchain.crypto.PubKey
import net.postchain.economy.common_proposal.revokeCommonProposalOperation
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.proposal.util.proposalIndexOption
import net.postchain.mc.compatibility.ApiCompatECV21.revokeProposalOperationECV20
import net.postchain.mc.compatibility.ApiCompatECV57.revokeCommonProposalOperation
import net.postchain.mc.compatibility.ApiCompatECV57.revokeCommonProposalV65Operation

class CommandRevokeProposal : ECBaseCommand(
        name = "revoke",
        help = "Revoke/remove a proposal submitted by you"
) {
    private val idx by proposalIndexOption().required()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        when {
            ecVersion.version < ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION -> {
                transactionBuilder()
                        .revokeProposalOperationECV20(clientProviderPubkey, RowId(idx))
                        .postOrSave()
                        .printResult(
                                "Proposal revoked successfully",
                                "Cannot revoke proposal"
                        )
            }
            ecVersion.version < ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION -> {
                transactionBuilder()
                        .revokeCommonProposalOperation(PubKey(clientProviderPubkey), RowId(idx))
                        .postOrSave()
                        .printResult(
                                "Proposal revoked successfully",
                                "Cannot revoke proposal"
                        )
            }
            ecVersion.version < ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION -> {
                transactionBuilder()
                        .revokeCommonProposalV65Operation(RowId(idx))
                        .postOrSave()
                        .printResult(
                                "Proposal revoked successfully",
                                "Cannot revoke proposal"
                        )
            }
            else -> {
                transactionBuilder()
                        .revokeCommonProposalOperation(clientProviderPubkey, RowId(idx))
                        .postOrSave()
                        .printResult(
                                "Proposal revoked successfully",
                                "Cannot revoke proposal"
                        )
            }
        }
    }
}
