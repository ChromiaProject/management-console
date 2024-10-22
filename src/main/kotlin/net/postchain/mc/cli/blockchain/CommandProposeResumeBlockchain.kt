package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.BlockchainAction
import net.postchain.chain0.proposal_blockchain.proposeBlockchainActionOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeResumeBlockchain : DCBaseCommand(
        name = "start",
        help = "Propose starting a blockchain that has previously been stopped"
) {
    private val blockchainRID by blockchainRidOption().required()

    private val description by proposalDescriptionOption { "Start blockchain $blockchainRID" }

    override fun runDC() {
        client.transactionBuilder()
                .proposeBlockchainActionOperation(
                        clientProviderPubkey,
                        blockchainRID,
                        BlockchainAction.resume,
                        description
                )
                .postAwaitConfirmation()
                .printResult(
                        "Blockchain resume proposition was added successfully",
                        "Cannot add proposal for resuming blockchain"
                )
    }
}