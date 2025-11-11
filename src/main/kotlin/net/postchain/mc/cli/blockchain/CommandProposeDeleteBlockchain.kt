package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.BlockchainAction
import net.postchain.chain0.proposal_blockchain.proposeBlockchainActionOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeDeleteBlockchain : DCBaseCommand(
        name = "remove",
        help = "Propose removal of blockchain. Command is irreversible"
) {
    private val blockchainRID by blockchainRidOption().required()

    private val description by proposalDescriptionOption { "Remove blockchain $blockchainRID" }

    override fun runDC() {
        transactionBuilder()
                .proposeBlockchainActionOperation(
                        clientProviderPubkey,
                        blockchainRID,
                        BlockchainAction.remove,
                        description
                )
                .postOrSave()
                .printResult(
                        "Blockchain delete proposition was added successfully",
                        "Cannot add proposal for deleting blockchain"
                )
    }
}