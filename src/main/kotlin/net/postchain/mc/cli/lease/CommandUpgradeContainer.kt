package net.postchain.mc.cli.lease

import com.chromia.cli.tools.ft.addEvmAuthOperation
import com.chromia.cli.tools.ft.addFtAuthOperation
import com.chromia.cli.tools.ft.findFtAccountIdWithAuthDescriptorId
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import net.postchain.client.core.PostchainClient
import net.postchain.common.hexStringToByteArray
import net.postchain.common.tx.TransactionStatus
import net.postchain.economy.economy_chain.CREATE_CONTAINER_WITH_SUBNODE_IMAGE
import net.postchain.economy.economy_chain.TicketState
import net.postchain.economy.economy_chain.UPGRADE_CONTAINER
import net.postchain.economy.economy_chain.getUpgradeContainerTicketById
import net.postchain.economy.economy_chain.getUpgradeContainerTicketByTransaction
import net.postchain.economy.economy_chain.upgradeContainerOperation
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.accountIdOption
import net.postchain.mc.cli.base.ECONOMY_CHAIN_COMPUTE_REQUESTS
import net.postchain.mc.cli.optionalEvmAddressOption
import net.postchain.mc.compatibility.ApiCompatECV59.upgradeContainerOperationV59

class CommandUpgradeContainer : ECBaseCommand(
        name = "upgrade-container",
        help = "Upgrade a container",
) {
    val accountIdOption by accountIdOption()

    val evmAddress by optionalEvmAddressOption()

    val containerName by option("-n", "--name", help = "Container name", metavar = "name").required()

    val scus by option("--scus", help = "Number of SCUs", metavar = "SCUs").int().required().validate {
        require(it > 0) { "SCUs must be greater than 0" }
    }

    val duration by option("--duration", metavar = "weeks").int().required().validate {
        require(it > 0) { "Duration must be greater than 0" }
    }

    val extraStorage by option("--extraStorage", metavar = "GiB").int().default(0).validate {
        require(it >= 0) { "Extra storage cannot be negative" }
    }

    val extraComputeRequests by option("--extraComputeRequests").int().validate {
        require(it >= 0) { "Extra compute requests cannot be negative" }
    }

    val clusterName by option("-cn", "--cluster-name", help = "Name of the cluster").required()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        if (ecVersion.version < ECONOMY_CHAIN_COMPUTE_REQUESTS && extraComputeRequests != null) {
            throw CliktError("This version of Economy chain does not support extra compute requests")
        }

        val (accountId, authDescriptorId) =
                findFtAccountIdWithAuthDescriptorId(economyChainClient, accountIdOption,
                        evmAddress ?: client.config.signers.first().pubKey.data,
                        CREATE_CONTAINER_WITH_SUBNODE_IMAGE, null)

        val transactionResult = economyChainClient.transactionBuilder().also {
            evmAddress?.let { evmAddress ->
                addEvmAuthOperation(
                        economyChainClient, it,
                        UPGRADE_CONTAINER, listOf(
                        gtv(containerName),
                        gtv(scus.toLong()),
                        gtv(extraStorage.toLong()),
                        gtv(clusterName),
                        gtv(duration.toLong()),
                ),
                        evmAddress, accountId, authDescriptorId)
                echo("Signing done, posting transaction...")
            } ?: run {
                addFtAuthOperation(it, accountId, authDescriptorId)
            }

        }.let {
                    if (ecVersion.version < ECONOMY_CHAIN_COMPUTE_REQUESTS) {
                        it.upgradeContainerOperationV59(
                                containerName = containerName,
                                upgradedContainerUnits = scus.toLong(),
                                upgradedExtraStorageGib = extraStorage.toLong(),
                                upgradedClusterName = clusterName,
                                upgradedDurationWeeks = duration.toLong(),
                        )
                    } else {
                        it.upgradeContainerOperation(
                                containerName = containerName,
                                upgradedContainerUnits = scus.toLong(),
                                upgradedExtraStorageGib = extraStorage.toLong(),
                                upgradedClusterName = clusterName,
                                upgradedDurationWeeks = duration.toLong(),
                                upgradedExtraComputeRequests = extraComputeRequests?.toLong() ?: 0L,
                        )
                    }
                }
                .postAwaitConfirmation(txListener())
        when (transactionResult.status) {
            TransactionStatus.CONFIRMED -> {
                val ticket = economyChainClient.getUpgradeContainerTicketByTransaction(transactionResult.txRid.rid.hexStringToByteArray())
                if (ticket != null) {
                    echo("Container upgrade request created, ticket id: ${ticket.ticketId} for TxRID: ${transactionResult.txRid.rid}")
                    repeat(60) {
                        Thread.sleep(1000)
                        val newTicket = economyChainClient.getUpgradeContainerTicketById(ticket.ticketId)
                        if (newTicket != null) {
                            when (newTicket.state) {
                                TicketState.SUCCESS -> {
                                    echo("Container ${newTicket.containerName} upgraded successfully")
                                    return
                                }
                                TicketState.FAILURE -> {
                                    echo("Container upgrade failed: ${newTicket.errorMessage}")
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

            TransactionStatus.REJECTED -> throw CliktError("Failed to upgrade container: ${transactionResult.rejectReason}")

            TransactionStatus.WAITING ->
                throw PrintMessage("Transaction was sent to transaction queue - TxRID: ${transactionResult.txRid.rid}", statusCode = 2)

            else -> throw CliktError("Cannot find status for this transaction")
        }
    }
}
