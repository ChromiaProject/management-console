package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.rendering.TextAlign
import net.postchain.economy.economy_chain.getEconomyMetrics
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.cli.util.pubkeyOption

class CommandGetEconomyMetrics : CliktCommand(
        name = "metrics",
        help = "Economy metrics for a provider"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val key by pubkeyOption()

    override fun run() {
        val economyChainClient = getEconomyChainClient(client, config.config)
        val economyMetrics = economyChainClient.getEconomyMetrics(key)
        echo(pmcTable {
            captionTop("Economy metrics for provider", TextAlign.LEFT)
            body {
                row("Nodes average availability", economyMetrics.averageAvailabilityOnAProvidersNodes)
                row("Total number of nodes", economyMetrics.totalNumberOfNodesPerProvider)
                row("Total number of SCUs", economyMetrics.totalNumberOfScusPerProvider)
                row("Average occupancy rate of SCUs", economyMetrics.averageOccupancyRateOfAProvidersScus)
                row("Total reward payed in the last 24h", economyMetrics.amountOfTheLastRewardPayout)
            }
        })
    }
}