package net.postchain.mc.cli.lease

import com.chromia.cli.tools.ft.addEvmAuthOperation
import com.chromia.cli.tools.ft.findFtAccountIdWithAuthDescriptorId
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.client.core.PostchainClient
import net.postchain.common.hexStringToByteArray
import net.postchain.common.tx.TransactionStatus
import net.postchain.economy.economy_chain.TicketState
import net.postchain.economy.economy_chain_remove_container.REMOVE_CONTAINER
import net.postchain.economy.economy_chain_remove_container.getRemoveContainerTicketById
import net.postchain.economy.economy_chain_remove_container.getRemoveContainerTicketByTransaction
import net.postchain.economy.economy_chain_remove_container.removeContainerOperation
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.accountIdOption
import net.postchain.mc.cli.evmAddressOption

class CommandRemoveContainer : ECBaseCommand(
        name = "remove-container",
        help = "Remove a container and its associated lease without refund"
) {
    val accountIdOption by accountIdOption()

    val evmAddress by evmAddressOption()

    val containerName by option("-n", "--name", help = "Container name", metavar = "name").required()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        val (accountId, authDescriptorId) =
                findFtAccountIdWithAuthDescriptorId(economyChainClient, accountIdOption, evmAddress, REMOVE_CONTAINER, null)

        val transactionResult = economyChainClient.transactionBuilder().also {
            addEvmAuthOperation(
                    economyChainClient, it,
                    REMOVE_CONTAINER, listOf(gtv(containerName)),
                    evmAddress, accountId, authDescriptorId)
            echo("Signing done, posting transaction...")
        }
                .removeContainerOperation(containerName)
                .postAwaitConfirmation()
        when (transactionResult.status) {
            TransactionStatus.CONFIRMED -> {
                val ticket = economyChainClient.getRemoveContainerTicketByTransaction(transactionResult.txRid.rid.hexStringToByteArray())
                if (ticket != null) {
                    echo("Container removal request created, ticket id: ${ticket.ticketId} for TxRID: ${transactionResult.txRid.rid}")
                    repeat(60) {
                        Thread.sleep(1000)
                        val newTicket = economyChainClient.getRemoveContainerTicketById(ticket.ticketId)
                        if (newTicket != null) {
                            when (newTicket.state) {
                                TicketState.SUCCESS -> {
                                    echo("Container ${newTicket.containerName} removed successfully")
                                    return
                                }
                                TicketState.FAILURE -> {
                                    echo("Container removal failed: ${newTicket.errorMessage}")
                                    return
                                }
                                TicketState.PENDING -> {}
                            }
                        }
                    }
                } else {
                    throw CliktError("Failed to get ticket id for TxRID: ${transactionResult.txRid.rid}")
                }
            }

            TransactionStatus.REJECTED -> throw CliktError("Failed to remove container: ${transactionResult.rejectReason}")

            TransactionStatus.WAITING ->
                throw PrintMessage("Transaction was sent to transaction queue - TxRID: ${transactionResult.txRid.rid}", statusCode = 2)

            else -> throw CliktError("Cannot find status for this transaction")
        }
    }
}
