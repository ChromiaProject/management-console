package net.postchain.mc.cli.blockchain.import_chain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.proposal_blockchain_import.proposeFinishForeignBlockchainImportOperation
import net.postchain.client.core.TransactionResult
import net.postchain.common.tx.TransactionStatus
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.nopClientOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion

class CommandProposeFinishForeignImport : CliktCommand(
        name = "finish-foreign-import",
        help = """
            Propose finishing import of a foreign blockchain

            Change will be applied after voting within the deployer voter set 
            of the cluster that the container belongs to.
        """.trimIndent()
) {
    private val client by nopClientOption()

    private val blockchainRID by blockchainRidOption().required()

    private val finalHeight by option("--final-height").long().required()

    private val description by proposalDescriptionOption(default = "Propose finishing import of foreign blockchain")

    override fun run() {
        client.requireApiVersion(10)
        echo("Import of foreign blockchain ${blockchainRID.toHex()} will be finished")
        val txBuilder = client.transactionBuilder()
        txBuilder.proposeFinishForeignBlockchainImportOperation(client.pubkey, blockchainRID, finalHeight, description)
        txBuilder.postAwaitConfirmation().printResultPolitely()
    }

    private fun TransactionResult.printResultPolitely() {
        val onSuccess = "Import of foreign blockchain ${blockchainRID.toHex()} finished"
        val onFail = "Cannot finish foreign blockchain import"
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
