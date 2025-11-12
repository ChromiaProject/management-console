package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.approveRemoveForcedConfigurationOperation
import net.postchain.chain0.proposal_blockchain.getRemoveForcedConfigurationStage2Proposal
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchain.CommandProposeRemoveForcedConfiguration.Companion.DISABLE_CHECKS_LONG_OPTION_NAME
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
    private val disableChecks by option("-dc", DISABLE_CHECKS_LONG_OPTION_NAME, help = "Disable verification checks and ignore warnings").flag(default = false)

    override fun runDC() {
        if (!disableChecks) {
            val p = client.getRemoveForcedConfigurationStage2Proposal(blockchainRID)
                    ?: throw CliktError("No forced configuration removal proposal for blockchain $blockchainRID")
            validateRemoveForcedConfiguration(blockchainRID, p.height)
        }
        transactionBuilder()
                .approveRemoveForcedConfigurationOperation(clientProviderPubkey, blockchainRID)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Forced configuration removal was approved",
                        "Failed to approve forced configuration removal"
                )
    }
}
