package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktError
import net.postchain.chain0.economy_chain_in_directory_chain.getEconomyChainRid
import net.postchain.client.core.PostchainClient
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.util.PmcClientConfigOption
import net.postchain.mc.network.Version

const val DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION = 30L // The version of directory chain introducing economy chain
const val ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION = 21L // EC <= v20 use ec proposals, v21 > use common_proposals
const val ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION = 43L
const val ECONOMY_CHAIN_MINTING_VERSION = 22L // EC >= 22 has minting support
const val ECONOMY_CHAIN_STAKING_REQUIREMENTS_VERSION = 24L // EC >= 24 has staking requirements
const val ECONOMY_CHAIN_EC_CONSTANTS_AS_PROPOSALS_VERSION = 29L // EC >= 29 has changed EC constants to be proposals instead of admin controlled
const val ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION = 33L // EC >= 29 has changed EC constants to be proposals instead of admin controlled
const val ECONOMY_CHAIN_PRICE_ORACLE_RATE_PROPOSAL_VERSION = 34L // EC >= 34 has price oracle rate proposal
const val ECONOMY_CHAIN_STAKING_REQ_NODE_BASED_VERSION = 46L
const val ECONOMY_CHAIN_SCHEDULED_PROPOSAL_VERSION = 53L

const val UNITS_PER_CHR = 1_000_000
const val UNITS_PER_USD = 1_000_000

fun getEconomyChainClient(config: PmcClientConfigOption): PostchainClient {

    val version = Version(config.client).version
    if (version < DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION) {
        throw CliktError("Economy chain requires directory chain version $DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION, found version $version")
    }

    val economyChainBrid = config.client.getEconomyChainRid()
            ?: throw CliktError("Economy chain is not initialized")

    return if (config.lookupNodes)
        config.chromiaClient.getSystemChainClient(BlockchainRid(economyChainBrid), addNop = true)
    else
        config.chromiaClient.getSystemChainClientForForwardingReplica(BlockchainRid(economyChainBrid), addNop = true)
}

fun formatUsd(chr: Long?, ecVersion: Long): String = formatCurrency(chr, UNITS_PER_USD, ecVersion)

fun formatChr(chr: Long?, ecVersion: Long): String = formatCurrency(chr, UNITS_PER_CHR, ecVersion)

fun formatCurrency(value: Long?, units: Int, ecVersion: Long): String {

    if (value == null) {
        return ""
    }

    if (doEcSupportMinorUnits(ecVersion)) {
        return value.toBigDecimal().divide(units.toBigDecimal()).toString()
    }

    return value.toString()
}

fun doEcSupportMinorUnits(ecVersion: Long) =
        ecVersion >= ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION
