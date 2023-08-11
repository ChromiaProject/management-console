package net.postchain.mc.cli.blockchain.import_chain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain_import.proposeForeignBlockchainBlocksImportOperation
import net.postchain.client.core.TransactionResult
import net.postchain.common.tx.TransactionStatus
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.nopClientOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion

class CommandProposeImportForeignBlocks : CliktCommand(
        name = "import-foreign-blocks",
        help = """
            Propose importing a foreign blockchain blocks 

            Change will be applied after voting within the deployer voter set 
            of the cluster that the container belongs to.
        """.trimIndent()
) {
    private val client by nopClientOption()

    private val blockchainRID by blockchainRidOption().required()

    private val description by proposalDescriptionOption(default = "Propose import of foreign blockchain blocks")

    override fun run() {
        client.requireApiVersion(10)
        echo("Import of blocks of foreign blockchain ${blockchainRID.toHex()} will be proposed")
        val txBuilder = client.transactionBuilder()
        txBuilder.proposeForeignBlockchainBlocksImportOperation(client.pubkey, blockchainRID, description)
        txBuilder.postAwaitConfirmation().printResultPolitely()
    }

    private fun TransactionResult.printResultPolitely() {
        val onSuccess = "Import of blocks of foreign blockchain ${blockchainRID.toHex()} proposed"
        val onFail = "Cannot propose import of blocks of foreign blockchain"
        try {
            printResult(onSuccess, onFail)
        } catch (e: CliktError) {
            when (status) {
                TransactionStatus.CONFIRMED -> echo(onSuccess)
                else -> throw e
            }
        }
    }
}
