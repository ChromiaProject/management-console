package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.findBlockchainRid
import net.postchain.chain0.proposal_blockchain.proposeBlockchainOperation
import net.postchain.chain0.version.apiVersion
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.common.tx.TransactionStatus
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.blockchainConfigOption
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.nopClientOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeBlockchain : CliktCommand(
        name = "add",
        help = "Propose a new blockchain in a specific container. Change will be applied after voting within the deployer voter set of the cluster that the container belongs to."
) {
    private val client by nopClientOption()

    private val blockchainConfigFile by blockchainConfigOption().required()

    private val container by option("-c", "--container", help = "Name of container to run in").required()

    private val name by nameOption("Name of blockchain").required()

    private val quiet by option("-q", "--quiet", help = "Print only blockchain RID if succeeds").flag()

    private val description by proposalDescriptionOption()

    override fun run() {
        val apiVersion = client.apiVersion()
        val bcConfig = BlockchainConfig.readFromFile(blockchainConfigFile)
        val txResult = client.transactionBuilder()
                .proposeBlockchainOperation(client.pubkey, bcConfig.data, name, container, description)
                .postAwaitConfirmation()
                .apply {
                    when (status) {
                        TransactionStatus.CONFIRMED -> {}
                        TransactionStatus.REJECTED -> throw CliktError("Cannot add bc proposal: $rejectReason")
                        TransactionStatus.WAITING -> if (quiet) {
                            echo("Transaction not confirmed", err = true)
                            throw ProgramResult(0)
                        } else
                            throw PrintMessage("Transaction not confirmed")
                        else -> throw CliktError("Cannot find status for this transaction")
                    }
                }

        if (apiVersion >= 8) {
            val maybeBcRid: ByteArray? = client.findBlockchainRid(txResult.txRid.rid.hexStringToByteArray())
            if (maybeBcRid != null) {
                echo(if (quiet) maybeBcRid.toHex() else "Blockchain $name has been added, bc-rid: $maybeBcRid.toHex()}")
            } else {
                echo("Blockchain $name has been proposed, tx-rid: ${txResult.txRid.rid}", err = quiet)
            }
        } else {
            echo(if (quiet) bcConfig.hash.toHex() else "Blockchain $name has been proposed, bc-rid: ${bcConfig.hash.toHex()}")
        }
    }
}
