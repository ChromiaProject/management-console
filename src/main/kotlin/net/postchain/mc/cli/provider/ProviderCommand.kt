package net.postchain.mc.cli.provider

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.core.subcommands
import net.postchain.mc.cli.provider.keys.providerKeyCommands

class CommonProviderCommand : PmcCommand(
        name = "provider",
        help = "Provider commands"
) {
    override fun run() = Unit

    override fun aliases(): Map<String, List<String>> {
        return mapOf(
                "add" to listOf("register"),
                "keys" to listOf("key", "list"),
        )
    }
}

fun providerCommands() = CommonProviderCommand().subcommands(
        CommandGetProviderInfo(),
        CommandListProviderQuotas(),
        CommandProposeProviderQuota(),
        CommandListProviderNodes(),
        CommandListProviders(),
        CommandRegisterProvider(),
        CommandProposeEnableProvider(),
        CommandPromoteProvider(),
        CommandProposeDisableProvider(),
        CommandTransferActionPoints(),
        providerKeyCommands(),
)
