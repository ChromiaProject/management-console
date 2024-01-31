package net.postchain.mc.cli.economy

import com.chromia.cli.tools.config.ChromiaConfig
import com.github.ajalt.clikt.core.CliktError
import net.postchain.chain0.economy_chain_in_directory_chain.getEconomyChainRid
import net.postchain.client.core.PostchainClient
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.util.NopPostchainClient
import net.postchain.mc.network.Version

const val ECONOMY_CHAIN_VERSION = 30

fun getEconomyChainClient(directoryChainClient: PostchainClient, config: ChromiaConfig): PostchainClient {

    val version = Version(directoryChainClient).version
    if (version < ECONOMY_CHAIN_VERSION) {
        throw CliktError("Economy chain requires directory chain version $ECONOMY_CHAIN_VERSION, found version $version")
    }

    val economyChainBrid = directoryChainClient.getEconomyChainRid()
            ?: throw CliktError("Economy chain is not initialized")

    return NopPostchainClient(PostchainClientImpl(config.get(blockchainRid = BlockchainRid(economyChainBrid))))
}