package net.postchain.mc.network

import com.chromia.cli.tools.config.ChromiaConfig
import com.github.ajalt.clikt.core.CliktError
import net.postchain.chain0.ticketing.getTicketChainRid
import net.postchain.client.core.PostchainClient
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.base.printResult

fun initTicketChain(client: PostchainClient, config: ChromiaConfig) {
    val ticketChainRid = client.getTicketChainRid()
    if (ticketChainRid != null) {
        val ticketChainClient = PostchainClientImpl(config.get(blockchainRid = BlockchainRid(ticketChainRid)))
        ticketChainClient.transactionBuilder()
                .addOperation("init")
                .postAwaitConfirmation()
                .printResult(
                        "Ticket chain was initiated",
                        "Failed to initiate ticket chain",
                        printOnSuccess = false
                )
    } else {
        throw CliktError("""Ticket chain not yet available, please run "pmc network initialize-ticketing" after a while""")
    }
}
