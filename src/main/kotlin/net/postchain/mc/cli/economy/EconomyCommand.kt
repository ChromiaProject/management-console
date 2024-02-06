package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class EconomyCommand : CliktCommand("Economy chain commands") {
    override fun run() = Unit

    override fun aliases(): Map<String, List<String>> {
        return super.aliases() + mapOf(
                "clusters" to listOf("list-clusters"),
                "tags" to listOf("list-tags"),
        )
    }
}

fun economyCommands() = EconomyCommand().subcommands(
        CommandListTags(),
        CommandAddTag(),
        CommandUpdateTag(),
        CommandRemoveTag(),
        CommandAddCluster(),
        CommandListClusters(),
        CommandChangeClusterTag(),
        CommandClusterCreationStatus(),
        CommandVersion(),
        CommandGetEconomyMetrics()
)

