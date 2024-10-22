package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.BlockchainAction
import net.postchain.chain0.proposal_blockchain.proposeBlockchainActionOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeArchiveBlockchain : DCBaseCommand(
        name = "archive",
        help = "Propose archiving of blockchain. Command is irreversible",
        requiresVersion = 33
) {
    private val blockchainRID by blockchainRidOption().required()

    private val description by proposalDescriptionOption { "Archive blockchain $blockchainRID" }

    override fun runDC() {
        client.transactionBuilder()
                .proposeBlockchainActionOperation(
                        clientProviderPubkey,
                        blockchainRID,
                        BlockchainAction.archive,
                        description
                )
                .postAwaitConfirmation()
                .printResult(
                        "Blockchain archive proposition was added successfully",
                        "Cannot add proposal for archiving blockchain"
                )
    }
}