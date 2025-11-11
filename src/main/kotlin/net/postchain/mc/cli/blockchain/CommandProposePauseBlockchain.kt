package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.BlockchainAction
import net.postchain.chain0.proposal_blockchain.proposeBlockchainActionOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposePauseBlockchain : DCBaseCommand(
        name = "stop",
        help = "Propose stopping a blockchain from building blocks"
) {
    private val blockchainRID by blockchainRidOption().required()

    private val description by proposalDescriptionOption { "Stop blockchain $blockchainRID" }

    override fun runDC() {
        transactionBuilder()
                .proposeBlockchainActionOperation(
                        clientProviderPubkey,
                        blockchainRID,
                        BlockchainAction.pause,
                        description
                )
                .postOrSave()
                .printResult(
                        "Blockchain pause proposition was added successfully",
                        "Cannot add proposal for pausing blockchain"
                )
    }
}