package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktError
import net.postchain.chain0.economy_chain_in_directory_chain.getEconomyChainRid
import net.postchain.client.core.PostchainClient
import net.postchain.common.BlockchainRid
import net.postchain.economy.lib.ft4.core.accounts.AuthType
import net.postchain.gtv.Gtv
import net.postchain.mc.cli.base.DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION
import net.postchain.mc.cli.util.PmcClientConfigOption
import net.postchain.mc.network.Version

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

fun extractSignersFromAuthDescriptor(authType: AuthType, args: Gtv): List<ByteArray> = when (authType) {
    AuthType.S ->
        listOf(args.asArray()[1].asByteArray())

    AuthType.M ->
        args.asArray()[2].asArray().map { it.asByteArray() }
}
