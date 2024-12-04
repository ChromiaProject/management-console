package net.postchain.mc.network

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands

class NetworkCommand : NoOpCliktCommand() {
    override fun help(context: Context) = "Commands relating to the network as a whole"
}

fun networkCommands() = NetworkCommand().subcommands(
        CommandInit(),
        CommandInitEconomyChain(),
        CommandInitTokenChain(),
        CommandInitEvmTransactionSubmitterChain(),
        CommandInitEvmEventReceiverChain(),
        CommandInitEvmEventReceiverPriceOracleChain(),
        CommandInitEvmEventReceiverTokenChain(),
        CommandInitPriceOracleChain(),
        SummaryCommand(),
        CommandVersion(),
        VerifyCommand(),
)
