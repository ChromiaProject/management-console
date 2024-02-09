package net.postchain.mc.cli.economy

import com.github.ajalt.mordant.rendering.TextAlign
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.getEconomyMetrics
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.cli.util.pubkeyOption

class CommandGetEconomyMetrics : ECBaseCommand(
        name = "metrics",
        help = "Economy metrics for a provider"
) {
    private val key by pubkeyOption()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

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