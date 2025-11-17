package net.postchain.mc.cli.transaction

import com.github.ajalt.clikt.core.subcommands
import net.postchain.mc.cli.PmcCommand

class TransactionCommand : PmcCommand(help = "Manage transactions") {
    override fun run() = Unit
}

fun transactionCommands() = TransactionCommand().subcommands(
        CommandViewTransaction(),
        CommandSignTransaction(),
        CommandSendTransaction(),
)