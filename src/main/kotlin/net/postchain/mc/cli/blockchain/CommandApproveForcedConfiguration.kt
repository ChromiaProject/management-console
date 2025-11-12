package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.google.gson.Gson
import net.postchain.chain0.proposal_blockchain.approveProposedForcedConfigurationOperation
import net.postchain.chain0.proposal_blockchain.getForcedConfigurationStage2Proposal
import net.postchain.client.exception.NodesDisagree
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.client.impl.QueryMajorityRequestStrategyFactory
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption

class CommandApproveForcedConfiguration : DCBaseCommand(
        name = "approve-force-update",
        help = """
        Approve a forced configuration to a blockchain

        Can only be done by a node provider in the cluster running the blockchain.                
        
        WARNING!!! Only use this if absolutely necessary.
        Command is irreversible but forced configs can be overwritten.
        
        Specified height must be the current blockchain height in order that the blockchain to be able to be RESUMED successfully.
        WARNING!!! Using a different height could cause problems with replicating the blockchain.
        Signers lists will be updated with the current cluster's nodes.
        Pending configurations for this blockchain will be removed.
        """.trimIndent(),
        requiresVersion = 85
) {

    companion object {
        const val DISABLE_CHECKS_LONG_OPTION_NAME = "--disable-checks"
    }

    private val blockchainRID by blockchainRidOption().required()
    private val disableChecks by option("-dc", DISABLE_CHECKS_LONG_OPTION_NAME, help = "Disable verification checks and ignore warnings").flag(default = false)

    override fun runDC() {
        if (!disableChecks) {
            val p = client.getForcedConfigurationStage2Proposal(blockchainRID)
                    ?: throw CliktError("No forced configuration proposal for blockchain $blockchainRID")
            val chainClient = config.chromiaClient.getClient(blockchainRID, QueryMajorityRequestStrategyFactory())
            val nodesCurrentHeight = try {
                // Intentionally using generic function to force query to multiple nodes
                val getHeightResponse = chainClient.genericGetJson("/blockchain/$blockchainRID/height")
                parseHeightResponse(getHeightResponse)
            } catch (_: NodesDisagree) {
                throw CliktError("Unable to validate proposal since nodes disagree on the current block height, run command with $DISABLE_CHECKS_LONG_OPTION_NAME to ignore this warning.")
            }

            if (nodesCurrentHeight != p.proposedHeight) {
                throw CliktError("Current blockchain height is $nodesCurrentHeight, but configuration proposal is forced at height ${p.proposedHeight}, run command with $DISABLE_CHECKS_LONG_OPTION_NAME to ignore this warning.")
            }
        }

        transactionBuilder()
                .approveProposedForcedConfigurationOperation(clientProviderPubkey, blockchainRID)
                .postOrSave()
                .printResult(
                        "Forced configurations was approved",
                        "Failed to approve forced configuration"
                )
    }

    private fun parseHeightResponse(response: String): Long =
            Gson().fromJson(response, PostchainClientImpl.CurrentBlockHeight::class.java).blockHeight
}
