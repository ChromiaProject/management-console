package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.subcommands
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.economy.proposal.proposalCommands

class EconomyCommand : PmcCommand(help = "Economy chain commands") {
    override fun run() = Unit

    override fun aliases(): Map<String, List<String>> {
        return super.aliases() + mapOf(
                "clusters" to listOf("list-clusters"),
                "tags" to listOf("list-tags"),
                "proposals" to listOf("proposal", "list"),
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
        CommandGetEconomyMetrics(),
        CommandGetEconomyConstants(),
        CommandUpdateSystemProviderEconomyConstants(),
        CommandUpdateStakingEconomyConstants(),
        CommandUpdateEconomyConstants(),
        CommandAuthDescriptorEvmSwap(),
        CommandUpdatePriceOracleRates(),
        proposalCommands(),
        CommandSetProviderStakingAccount(),
        CommandForceRemoveContainer(),
        CommandClaimTestChr(),
)
