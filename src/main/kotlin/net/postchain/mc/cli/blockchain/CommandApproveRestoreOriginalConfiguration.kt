package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.approveRestoreOriginalConfigurationOperation
import net.postchain.chain0.proposal_blockchain.getRestoreOriginalConfigurationStage2Proposal
import net.postchain.gtv.GtvDecoder.decodeGtv
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchain.CommandProposeRemoveForcedConfiguration.Companion.DISABLE_CHECKS_LONG_OPTION_NAME
import net.postchain.mc.cli.blockchainRidOption

class CommandApproveRestoreOriginalConfiguration : DCBaseCommand(
        name = "approve-restore-original-configuration",
        help = """
        Approve restoration of an accidentally overridden configuration on a blockchain.

        Can only be done by a node provider in the cluster running the blockchain.                
        
        WARNING!!! Only use this if absolutely necessary.
        """.trimIndent(),
        requiresVersion = 104
) {
    private val blockchainRID by blockchainRidOption().required()
    private val disableChecks by option("-dc", DISABLE_CHECKS_LONG_OPTION_NAME, help = "Disable verification checks and ignore warnings").flag(default = false)

    override fun runDC() {
        if (!disableChecks) {
            val p = client.getRestoreOriginalConfigurationStage2Proposal(blockchainRID)
                    ?: throw CliktError("No proposal for restoring original configuration for blockchain $blockchainRID")
            validateRestoreOriginalConfiguration(blockchainRID, p.height, decodeGtv(p.originalConfiguration.data), p.originalSigners?.map { it.data })
        }
        transactionBuilder()
                .approveRestoreOriginalConfigurationOperation(clientProviderPubkey, blockchainRID)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Configuration restoration was approved",
                        "Failed to approve configuration restoration"
                )
    }
}
