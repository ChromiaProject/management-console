package net.postchain.mc.cli.economy

import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.getEconomyConstants
import net.postchain.mc.cli.util.pmcTable

class CommandGetEconomyConstants : ECBaseCommand(
        name = "get-constants",
        help = "Get economy chain constants"
) {
    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        val economyConstants = economyChainClient.getEconomyConstants()
        echo(pmcTable {
            captionTop("Economy chain constants")
            body {
                row("Min lease time weeks", economyConstants.minLeaseTimeWeeks)
                row("Max lease time weeks", economyConstants.maxLeaseTimeWeeks)
                row("CHR per USD", economyConstants.chrPerUsd)
                row("Total cost system providers", economyConstants.totalCostSystemProviders)
                row("System provider fee share", economyConstants.systemProviderFeeShare)
                row("Staking reward fee share", economyConstants.stakingRewardFeeShare)
                row("Chromia foundation fee share", economyConstants.chromiaFoundationFeeShare)
                row("Resource pool margin feee share", economyConstants.resourcePoolMarginFeeShare)
                row("System provider risk share", economyConstants.systemProviderRiskShare)
                row("Dapp provider risk share", economyConstants.dappProviderRiskShare)
            }
        })
    }
}