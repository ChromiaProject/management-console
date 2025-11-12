package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.approveRemoveForcedConfigurationOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption

class CommandApproveRemoveForcedConfiguration : DCBaseCommand(
        name = "approve-remove-force-update",
        help = """
        Approve removal of forced configuration(s) on a blockchain.
        Use this to remove incorrectly applied forced configurations.

        Can only be done by a node provider in the cluster running the blockchain.                
        
        WARNING!!! Only use this if absolutely necessary.
        Command is irreversible.
        """.trimIndent(),
        requiresVersion = 104
) {
    private val blockchainRID by blockchainRidOption().required()

    override fun runDC() {
        transactionBuilder()
                .approveRemoveForcedConfigurationOperation(clientProviderPubkey, blockchainRID)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Forced configuration removal was approved",
                        "Failed to approve forced configuration removal"
                )
    }
}
