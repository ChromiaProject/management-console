package net.postchain.mc.cli.token

import com.github.ajalt.clikt.core.subcommands
import net.postchain.mc.cli.PmcCommand

class TokenCommand : PmcCommand(help = "Token chain commands") {
    override fun run() = Unit
}

fun tokenCommands() = TokenCommand().subcommands(
        CommandLinkEvmEoaAccount(),
)
