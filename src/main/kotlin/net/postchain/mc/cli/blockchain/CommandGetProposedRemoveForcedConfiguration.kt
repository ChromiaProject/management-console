package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.getRemoveForcedConfigurationStage2Proposal
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.blockchainRidOption

class CommandGetProposedRemoveForcedConfiguration : DCBaseCommand(
        name = "get-proposed-remove-forced-configuration",
        help = "Get proposed forced configuration removal",
        requiresVersion = 104
) {
    private val blockchainRID by blockchainRidOption().required()

    override fun runDC() {
        val p = client.getRemoveForcedConfigurationStage2Proposal(blockchainRID)
        if (p != null) {
            echo("Remove forced configuration(s) for blockchain ${p.blockchainRid.toHex()} at height: ${p.height}")
        } else {
            echo("No forced configuration removal proposals for blockchain ${blockchainRID.toHex()}")
        }
    }
}
