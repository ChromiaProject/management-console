package net.postchain.mc.cli.lease

import com.chromia.cli.tools.ft.findAccountId
import com.github.ajalt.clikt.core.UsageError
import net.postchain.client.core.PostchainClient
import net.postchain.common.toHex
import net.postchain.economy.economy_chain.getPendingTickets
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.accountIdOption
import net.postchain.mc.cli.optionalEvmAddressOption
import net.postchain.mc.cli.util.pmcTable

class CommandListPendingLeaseTickets : ECBaseCommand(
        name = "list",
        help = "List pending lease tickets",
        requiresECVersion = 56,
) {
    val maybeAccountId by accountIdOption()
    val maybeEvmAddress by optionalEvmAddressOption()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        val accountId = maybeAccountId ?: findAccountId(economyChainClient, maybeEvmAddress ?: throw UsageError("Need to specify either account id or EVM address"))
        val tickets = economyChainClient.getPendingTickets(accountId)
        echo(pmcTable(
                "pending lease tickets for account ${accountId.toHex()}",
                listOf("Ticket id", "Operation"),
                tickets.map { listOf(it.ticketId.toString(), it.type.toString()) },
        ))
    }
}
