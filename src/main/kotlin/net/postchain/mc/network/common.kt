package net.postchain.mc.network

import com.chromia.build.tools.config.ChromiaClientConfig
import com.github.ajalt.clikt.core.CliktError
import net.postchain.chain0.economy_chain_in_directory_chain.getEconomyChainRid
import net.postchain.chain0.token_chain_in_directory_chain.getTokenChainRid
import net.postchain.client.core.PostchainClient
import net.postchain.client.impl.PostchainClientProviderImpl
import net.postchain.common.BlockchainRid
import net.postchain.economy.economy_chain.apiVersion
import net.postchain.economy.economy_chain.initOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.token.token_chain.initTokenChainOperation
import java.lang.Thread.sleep

fun initEconomyChain(client: PostchainClient, config: ChromiaClientConfig) {
    val economyChainRid = client.getEconomyChainRid()
    if (economyChainRid != null) {
        val economyChainClient = config.setBrid(BlockchainRid(economyChainRid)).client(PostchainClientProviderImpl())

        // Make sure postchain has attached model to REST API
        repeatUntilSuccessful {
            economyChainClient.apiVersion()
        }

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

fun initTokenChain(client: PostchainClient, config: ChromiaClientConfig) {
    val tokenChainRid = client.getTokenChainRid()
    if (tokenChainRid.isNotEmpty()) {
        val tokenChainClient = config.setBrid(BlockchainRid(tokenChainRid)).client(PostchainClientProviderImpl())

        // Make sure postchain has attached model to REST API
        repeatUntilSuccessful {
            tokenChainClient.apiVersion()
        }

        tokenChainClient.transactionBuilder()
                .initTokenChainOperation()
                .postAwaitConfirmation()
                .printResult(
                        "Token chain was initiated",
                        "Failed to initiate token chain",
                        printOnSuccess = false
                )
    } else {
        throw CliktError("""Token chain not yet available, please run "pmc network initialize-token-chain" after a while""")
    }
}

fun repeatUntilSuccessful(times: Int = 30, interval: Long = 500, function: (Int) -> Unit) {

    var exception: Exception? = null
    repeat(times) {
        try {
            function(it)
            return@repeatUntilSuccessful
        } catch (e: Exception) {
            exception = e
        }
        sleep(interval)
    }

    if (exception != null) {
        throw exception!!
    }
    throw CliktError("Command timed out")
}
