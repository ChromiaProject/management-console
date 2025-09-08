package net.postchain.mc.cli.lease

import com.chromia.cli.tools.ft.addEvmAuthOperation
import com.chromia.cli.tools.ft.addFtAuthOperation
import com.chromia.cli.tools.ft.findFtAccountIdWithAuthDescriptorId
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import net.postchain.client.core.PostchainClient
import net.postchain.common.hexStringToByteArray
import net.postchain.common.tx.TransactionStatus
import net.postchain.economy.economy_chain.CREATE_CONTAINER_WITH_SUBNODE_IMAGE
import net.postchain.economy.economy_chain.TicketState
import net.postchain.economy.economy_chain.createContainerWithSubnodeImageOperation
import net.postchain.economy.economy_chain.getCreateContainerTicketById
import net.postchain.economy.economy_chain.getCreateContainerTicketByTransaction
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.accountIdOption
import net.postchain.mc.cli.base.ECONOMY_CHAIN_COMPUTE_REQUESTS
import net.postchain.mc.cli.optionalEvmAddressOption
import net.postchain.mc.compatibility.ApiCompatECV59.createContainerWithSubnodeImageOperationV59

class CommandCreateContainer : ECBaseCommand(
        name = "create-container",
        help = "Create a container",
) {
    val accountIdOption by accountIdOption()

    val evmAddress by optionalEvmAddressOption()

    val clusterName by option("-cn", "--cluster-name", help = "Name of the cluster").required()

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

    val subnodeImageName by option("-sin", "--subnode-image-name", help = "Subnode image name").default("")

    val autoRenew by option("--auto-renew", help = "Auto renew").flag(default = false)

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        if (ecVersion.version < ECONOMY_CHAIN_COMPUTE_REQUESTS && extraComputeRequests != null) {
            throw CliktError("This version of Economy chain does not support extra compute requests")
        }

        val (accountId, authDescriptorId) =
                findFtAccountIdWithAuthDescriptorId(economyChainClient, accountIdOption,
                        evmAddress ?: client.config.signers.first().pubKey.data,
                        CREATE_CONTAINER_WITH_SUBNODE_IMAGE, null)

        val pubkey = economyChainClient.config.signers.first().pubKey

        val transactionResult = economyChainClient.transactionBuilder().also {
            evmAddress?.let { evmAddress ->
                addEvmAuthOperation(
                        economyChainClient, it,
                        CREATE_CONTAINER_WITH_SUBNODE_IMAGE, listOf(
                        gtv(pubkey.data),
                        gtv(scus.toLong()),
                        gtv(duration.toLong()),
                        gtv(extraStorage.toLong()),
                        gtv(clusterName),
                        gtv(autoRenew),
                        gtv(subnodeImageName)
                ),
                        evmAddress, accountId, authDescriptorId)
                echo("Signing done, posting transaction...", err = true)
            } ?: run {
                addFtAuthOperation(it, accountId, authDescriptorId)
            }
        }.let {
            if (ecVersion.version < ECONOMY_CHAIN_COMPUTE_REQUESTS) {
                it.createContainerWithSubnodeImageOperationV59(
                        providerPubkey = pubkey.data,
                        containerUnits = scus.toLong(),
                        durationWeeks = duration.toLong(),
                        extraStorageGib = extraStorage.toLong(),
                        clusterName = clusterName,
                        autoRenew = autoRenew,
                        subnodeImageName = subnodeImageName)
            } else {
                it.createContainerWithSubnodeImageOperation(
                        providerPubkey = pubkey.data,
                        containerUnits = scus.toLong(),
                        durationWeeks = duration.toLong(),
                        extraStorageGib = extraStorage.toLong(),
                        clusterName = clusterName,
                        autoRenew = autoRenew,
                        subnodeImageName = subnodeImageName,
                        extraComputeRequests = extraComputeRequests?.toLong() ?: 0L)
            }
        }
                .postAwaitConfirmation(txListener())
        when (transactionResult.status) {
            TransactionStatus.CONFIRMED -> {
                val ticket = economyChainClient.getCreateContainerTicketByTransaction(transactionResult.txRid.rid.hexStringToByteArray())
                if (ticket != null) {
                    echo("Container creation request created, ticket id: ${ticket.ticketId} for TxRID: ${transactionResult.txRid.rid}")
                    repeat(60) {
                        Thread.sleep(1000)
                        val newTicket = economyChainClient.getCreateContainerTicketById(ticket.ticketId)
                        if (newTicket != null) {
                            when (newTicket.state) {
                                TicketState.SUCCESS -> {
                                    echo("Container ${newTicket.containerName} created successfully")
                                    return
                                }
                                TicketState.FAILURE -> {
                                    echo("Container creation failed: ${newTicket.errorMessage}")
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

            TransactionStatus.REJECTED -> throw CliktError("Failed to lease container: ${transactionResult.rejectReason}")

            TransactionStatus.WAITING ->
                throw PrintMessage("Transaction was sent to transaction queue - TxRID: ${transactionResult.txRid.rid}", statusCode = 2)

            else -> throw CliktError("Cannot find status for this transaction")
        }
    }
}
