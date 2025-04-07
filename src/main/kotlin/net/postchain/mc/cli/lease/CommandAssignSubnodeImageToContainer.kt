package net.postchain.mc.cli.lease

import com.chromia.cli.tools.ft.addEvmAuthOperation
import com.chromia.cli.tools.ft.addFtAuthOperation
import com.chromia.cli.tools.ft.findFtAccountIdWithAuthDescriptorId
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.client.core.PostchainClient
import net.postchain.common.hexStringToByteArray
import net.postchain.common.tx.TransactionStatus
import net.postchain.economy.economy_chain.ASSIGN_SUBNODE_IMAGE_TO_CONTAINER
import net.postchain.economy.economy_chain.CREATE_CONTAINER_WITH_SUBNODE_IMAGE
import net.postchain.economy.economy_chain.TicketState
import net.postchain.economy.economy_chain.assignSubnodeImageToContainerOperation
import net.postchain.economy.economy_chain.getAssignSubnodeImageToContainerTicketById
import net.postchain.economy.economy_chain.getAssignSubnodeImageToContainerTicketByTransaction
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.accountIdOption
import net.postchain.mc.cli.optionalEvmAddressOption

class CommandAssignSubnodeImageToContainer : ECBaseCommand(
        name = "assign-subnode-image",
        help = "Assign a custom subnode image to an existing container",
) {
    val accountIdOption by accountIdOption()

    val evmAddress by optionalEvmAddressOption()

    val containerName by option("-n", "--name", help = "Container name", metavar = "name").required()

    val subnodeImageName by option("-sin", "--subnode-image-name", help = "Subnode image name").default("")

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        val (accountId, authDescriptorId) =
                findFtAccountIdWithAuthDescriptorId(economyChainClient, accountIdOption,
                        evmAddress ?: client.config.signers.first().pubKey.data,
                        CREATE_CONTAINER_WITH_SUBNODE_IMAGE, null)

        val transactionResult = economyChainClient.transactionBuilder().also {
            evmAddress?.let { evmAddress ->
                addEvmAuthOperation(
                        economyChainClient, it,
                        ASSIGN_SUBNODE_IMAGE_TO_CONTAINER, listOf(
                        gtv(containerName),
                        gtv(subnodeImageName)
                ),
                        evmAddress, accountId, authDescriptorId)
                echo("Signing done, posting transaction...")
            } ?: run {
                addFtAuthOperation(it, accountId, authDescriptorId)
            }
        }
                .assignSubnodeImageToContainerOperation(containerName, subnodeImageName)
                .postAwaitConfirmation()
        when (transactionResult.status) {
            TransactionStatus.CONFIRMED -> {
                val ticket = economyChainClient.getAssignSubnodeImageToContainerTicketByTransaction(transactionResult.txRid.rid.hexStringToByteArray())
                if (ticket != null) {
                    echo("Subnode image assignment request created, ticket id: ${ticket.ticketId} for TxRID: ${transactionResult.txRid.rid}")
                    repeat(60) {
                        Thread.sleep(1000)
                        val newTicket = economyChainClient.getAssignSubnodeImageToContainerTicketById(ticket.ticketId)
                        if (newTicket != null) {
                            when (newTicket.state) {
                                TicketState.SUCCESS -> {
                                    echo("Subnode image assigned to container ${ticket.containerName} successfully")
                                    return
                                }
                                TicketState.FAILURE -> {
                                    echo("Subnode image assignment failed: ${newTicket.errorMessage}")
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

            TransactionStatus.REJECTED -> throw CliktError("Failed to assign subnode image: ${transactionResult.rejectReason}")

            TransactionStatus.WAITING ->
                throw PrintMessage("Transaction was sent to transaction queue - TxRID: ${transactionResult.txRid.rid}", statusCode = 2)

            else -> throw CliktError("Cannot find status for this transaction")
        }
    }
}
