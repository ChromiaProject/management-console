package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.approveRestoreOriginalConfigurationOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
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

    override fun runDC() {
        transactionBuilder()
                .approveRestoreOriginalConfigurationOperation(clientProviderPubkey, blockchainRID)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Configuration restoration was approved",
                        "Failed to approve configuration restoration"
                )
    }
}
