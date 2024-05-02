package net.postchain.mc.network

import com.chromia.build.tools.config.ChromiaClientConfig
import com.github.ajalt.clikt.core.CliktError
import net.postchain.chain0.economy_chain_in_directory_chain.getEconomyChainRid
import net.postchain.client.core.PostchainClient
import net.postchain.client.impl.PostchainClientProviderImpl
import net.postchain.common.BlockchainRid
import net.postchain.economy.economy_chain.initOperation
import net.postchain.mc.cli.base.printResult

fun initEconomyChain(client: PostchainClient, config: ChromiaClientConfig) {
    val economyChainRid = client.getEconomyChainRid()
    if (economyChainRid != null) {
        val economyChainClient = config.setBrid(BlockchainRid(economyChainRid)).client(PostchainClientProviderImpl())
        economyChainClient.transactionBuilder()
                .initOperation()
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
