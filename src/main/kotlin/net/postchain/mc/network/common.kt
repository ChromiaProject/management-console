package net.postchain.mc.network

import com.chromia.cli.tools.config.ChromiaConfig
import com.github.ajalt.clikt.core.CliktError
import net.postchain.chain0.economy_chain_in_directory_chain.getEconomyChainRid
import net.postchain.client.core.PostchainClient
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.base.printResult

fun initEconomyChain(client: PostchainClient, config: ChromiaConfig) {
    val economyChainRid = client.getEconomyChainRid()
    if (economyChainRid != null) {
        val economyChainClient = PostchainClientImpl(config.get(blockchainRid = BlockchainRid(economyChainRid)))
        economyChainClient.transactionBuilder()
                .addOperation("init")
                .postAwaitConfirmation()
                .printResult(
                        "Economy chain was initiated",
                        "Failed to initiate economy chain",
                        printOnSuccess = false
                )
    } else {
        throw CliktError("""Economy chain not yet available, please run "pmc network initialize-economy-chain" after a while""")
    }
}
