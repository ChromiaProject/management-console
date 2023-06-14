package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import net.postchain.chain0.proposal_blockchain.proposeFinishImportBlockchainOperation
import net.postchain.client.core.TransactionResult
import net.postchain.common.tx.TransactionStatus
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.nopClientOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.requireApiVersion

class CommandProposeFinishBlockchainImport : CliktCommand(
        name = "finish-import",
        help = "Propose finishing import of a blockchain. Change will be applied after voting within the deployer voter set of the cluster that the container belongs to.",
        hidden = true
) {
    private val client by nopClientOption()

    private val blockchainRID by blockchainRidOption()

    private val description by proposalDescriptionOption(default = "Propose finishing import of blockchain")

    override fun run() {
        client.requireApiVersion(5)
        echo("Import of blockchain ${blockchainRID.toHex()} will be finished")
        val txBuilder = client.transactionBuilder()
        txBuilder.proposeFinishImportBlockchainOperation(
                client.pubkey, blockchainRID, description)
        txBuilder.postAwaitConfirmation().printResultPolitely()
    }

    private fun TransactionResult.printResultPolitely() {
        val onSuccess = "Import of blockchain ${blockchainRID.toHex()} finished"
        val onFail = "Cannot finish blockchain import"
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
