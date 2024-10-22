package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.proposal_blockchain.proposeBlockchainUnarchiveActionOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeUnarchiveBlockchain : DCBaseCommand(
        name = "unarchive",
        help = "Propose unarchiving of blockchain. Command is irreversible",
        requiresVersion = 33
) {
    private val blockchainRID by blockchainRidOption().required()

    private val finalHeight by option("--final-height",
            help = "Unarchive blockchain blocks up to and including this height"
    ).long().required().validate {
        require(it > 0) { "--final-height arg must be greater than 0" }
    }

    private val destinationContainer by option("-dc", "--destination-container", help = "Name of container to unarchive blockchain to").required()

    private val description by proposalDescriptionOption {
        "Unarchive blockchain $blockchainRID to the container $destinationContainer with final-height: $finalHeight"
    }

    override fun runDC() {
        client.transactionBuilder()
                .proposeBlockchainUnarchiveActionOperation(
                        clientProviderPubkey,
                        blockchainRID,
                        destinationContainer,
                        finalHeight,
                        description
                )
                .postAwaitConfirmation()
                .printResult(
                        "Blockchain unarchive proposition was added successfully",
                        "Cannot add proposal for unarchiving blockchain"
                )
    }
}