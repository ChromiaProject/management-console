package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.BlockchainAction
import net.postchain.chain0.proposal_blockchain.proposeBlockchainActionOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposePauseBlockchain : CliktCommand(
        name = "stop",
        help = "Propose stopping a blockchain from building blocks"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val description by proposalDescriptionOption { "Stop blockchain $blockchainRID" }

    override fun run() {
        client.transactionBuilder()
                .proposeBlockchainActionOperation(
                        client.config.pubkey().data,
                        blockchainRID,
                        BlockchainAction.pause,
                        description
                )
                .postAwaitConfirmation()
                .printResult(
                        "Blockchain pause proposition was added successfully",
                        "Cannot add proposal for pausing blockchain"
                )
    }
}