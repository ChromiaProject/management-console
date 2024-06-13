package net.postchain.mc.cli.economy

import com.chromia.build.tools.config.ChromiaClientConfig
import com.github.ajalt.clikt.core.CliktError
import net.postchain.chain0.economy_chain_in_directory_chain.getEconomyChainRid
import net.postchain.client.core.PostchainClient
import net.postchain.client.impl.PostchainClientProviderImpl
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.util.NopPostchainClient
import net.postchain.mc.network.Version

const val DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION = 30 // The version of directory chain introducing economy chain
const val ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION = 21L // EC <= v20 use ec proposals, v21 > use common_proposals
const val ECONOMY_CHAIN_MINTING_VERSION = 22L // EC >= 22 has minting support
const val ECONOMY_CHAIN_STAKING_REQUIREMENTS_VERSION = 24L // EC >= 24 has staking requirements
const val ECONOMY_CHAIN_EC_CONSTANTS_AS_PROPOSALS_VERSION = 29L // EC >= 29 has changed EC constants to be proposals instead of admin controlled

fun getEconomyChainClient(directoryChainClient: PostchainClient, config: ChromiaClientConfig): PostchainClient {

    val version = Version(directoryChainClient).version
    if (version < DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION) {
        throw CliktError("Economy chain requires directory chain version $DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION, found version $version")
    }

    val economyChainBrid = directoryChainClient.getEconomyChainRid()
            ?: throw CliktError("Economy chain is not initialized")

    return NopPostchainClient(config.setBrid(BlockchainRid(economyChainBrid)).client(PostchainClientProviderImpl()))
}