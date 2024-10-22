package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.proposal_blockchain.proposeBlockchainRenameOperation
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeRenameBlockchain : DCBaseCommand(
        name = "rename",
        help = "Propose renaming a blockchain."
) {
    private val blockchainRID by blockchainRidOption().required()

    private val name by nameOption("Name of blockchain").required().validate(entityNameValidator())

    private val description by proposalDescriptionOption { "Rename blockchain $blockchainRID to $name" }

    override fun runDC() {

        val client = config.client
        val apiVersion = client.apiVersion()
        if (apiVersion < 61) {
            throw CliktError("Blockchain rename operation requires directory chain version 61, found version $apiVersion")
        }

        client.transactionBuilder()
                .proposeBlockchainRenameOperation(
                        clientProviderPubkey,
                        blockchainRID,
                        name,
                        description
                )
                .postAwaitConfirmation()
                .printResult(
                        "Blockchain rename proposition was added successfully",
                        "Cannot add proposal for renaming blockchain"
                )
    }
}