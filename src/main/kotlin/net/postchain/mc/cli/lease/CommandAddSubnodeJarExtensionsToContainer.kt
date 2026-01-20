package net.postchain.mc.cli.lease

import com.chromia.cli.tools.ft.addEvmAuthOperation
import com.chromia.cli.tools.ft.addFtAuthOperation
import com.chromia.cli.tools.ft.findFtAccountIdWithAuthDescriptorId
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import net.postchain.client.core.PostchainClient
import net.postchain.common.hexStringToByteArray
import net.postchain.common.tx.TransactionStatus
import net.postchain.economy.economy_chain.ADD_SUBNODE_JAR_EXTENSIONS_TO_CONTAINER
import net.postchain.economy.economy_chain.TicketState
import net.postchain.economy.economy_chain.addSubnodeJarExtensionsToContainerOperation
import net.postchain.economy.economy_chain.getAddSubnodeJarExtensionsToContainerTicketById
import net.postchain.economy.economy_chain.getAddSubnodeJarExtensionsToContainerTicketByTransaction
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.accountIdOption
import net.postchain.mc.cli.optionalEvmAddressOption

class CommandAddSubnodeJarExtensionsToContainer : ECBaseCommand(
        name = "add-subnode-jar-extensions",
        help = "Add subnode JAR extensions to an existing container",
) {
    val accountIdOption by accountIdOption()

    val evmAddress by optionalEvmAddressOption()

    val containerName by option("-n", "--name", help = "Container name", metavar = "name").required()

    val subnodeJarExtensionNames by option("-sje", "--subnode-jar-extension-names", help = "Comma separated list of subnode JAR extension names")
            .split(",").default(emptyList())

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        val (accountId, authDescriptorId) =
                findFtAccountIdWithAuthDescriptorId(economyChainClient, accountIdOption,
                        evmAddress ?: client.config.signers.first().pubKey.data,
                        ADD_SUBNODE_JAR_EXTENSIONS_TO_CONTAINER, null)

        val transactionResult = economyChainClient.transactionBuilder().also { txBuilder ->
            evmAddress?.let { evmAddress ->
                addEvmAuthOperation(
                        economyChainClient, txBuilder,
                        ADD_SUBNODE_JAR_EXTENSIONS_TO_CONTAINER, listOf(
                        gtv(containerName),
                        gtv(subnodeJarExtensionNames.map { gtv(it) })
                ),
                        evmAddress, accountId, authDescriptorId)
                echo("Signing done, posting transaction...", err = true)
            } ?: run {
                addFtAuthOperation(txBuilder, accountId, authDescriptorId)
            }
        }
                .addSubnodeJarExtensionsToContainerOperation(containerName, subnodeJarExtensionNames)
                .postAwaitConfirmation(txListener())
        when (transactionResult.status) {
            TransactionStatus.CONFIRMED -> {
                val ticket = economyChainClient.getAddSubnodeJarExtensionsToContainerTicketByTransaction(transactionResult.txRid.rid.hexStringToByteArray())
                if (ticket != null) {
                    echo("Subnode JAR extension addment request created, ticket id: ${ticket.ticketId} for TxRID: ${transactionResult.txRid.rid}")
                    repeat(60) {
                        Thread.sleep(1000)
                        val newTicket = economyChainClient.getAddSubnodeJarExtensionsToContainerTicketById(ticket.ticketId)
                        if (newTicket != null) {
                            when (newTicket.state) {
                                TicketState.SUCCESS -> {
                                    echo("Subnode JAR extension added to container ${ticket.containerName} successfully")
                                    return
                                }

                                TicketState.FAILURE -> {
                                    echo("Subnode JAR extension addment failed: ${newTicket.errorMessage}")
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

            TransactionStatus.REJECTED -> throw CliktError("Failed to add subnode JAR extensions: ${transactionResult.rejectReason}")

            TransactionStatus.WAITING ->
                throw PrintMessage("Transaction was sent to transaction queue - TxRID: ${transactionResult.txRid.rid}", statusCode = 2)

            else -> throw CliktError("Cannot find status for this transaction")
        }
    }
}
