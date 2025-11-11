package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.proposal_blockchain.proposeBlockchainRenameOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeRenameBlockchain : DCBaseCommand(
        name = "rename",
        help = "Propose renaming a blockchain.",
        requiresVersion = 61,
) {
    private val blockchainRID by blockchainRidOption().required()

    private val name by nameOption("Name of blockchain").required().validate(entityNameValidator())

    private val description by proposalDescriptionOption { "Rename blockchain $blockchainRID to $name" }

    override fun runDC() {
        transactionBuilder()
                .proposeBlockchainRenameOperation(
                        clientProviderPubkey,
                        blockchainRID,
                        name,
                        description
                )
                .postOrSave()
                .printResult(
                        "Blockchain rename proposition was added successfully",
                        "Cannot add proposal for renaming blockchain"
                )
    }
}