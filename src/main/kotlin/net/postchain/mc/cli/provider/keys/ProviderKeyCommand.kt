package net.postchain.mc.cli.provider.keys

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands

class ProviderKeyCommand : NoOpCliktCommand(name = "key") {
    override fun help(context: Context) = "Manage provider keys"
}

fun providerKeyCommands() = ProviderKeyCommand().subcommands(
        CommandListProviderKeys(),
        CommandAddProviderKey(),
        CommandRevokeProviderKey(),
        CommandSetProviderKeyThreshold(),
)
