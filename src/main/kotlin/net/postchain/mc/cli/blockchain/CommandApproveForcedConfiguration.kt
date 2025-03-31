package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.approveProposedForcedConfigurationOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption

class CommandApproveForcedConfiguration : DCBaseCommand(
        name = "approve-force-update",
        help = """
        Approve a forced configuration to a blockchain. 
        Can only be done by a node provider in the cluster running the blockchain.                
        
        WARNING!!! Only use this if absolutely necessary.
        Command is irreversible but forced configs can be overwritten.
        
        Specified height must be the current blockchain height in order that the blockchain to be able to be RESUMED successfully.
        WARNING!!! Using a different height could cause problems with replicating the blockchain.
        Signers lists will be updated with the current cluster's nodes.
        Pending configurations for this blockchain will be removed.
        """.trimIndent(),
        requiresVersion = 83
) {
    private val blockchainRID by blockchainRidOption().required()

    override fun runDC() {
        client.transactionBuilder()
                .approveProposedForcedConfigurationOperation(blockchainRID)
                .postAwaitConfirmation()
                .printResult(
                        "Forced configurations was approved",
                        "Failed to approve forced configuration"
                )
    }
}
