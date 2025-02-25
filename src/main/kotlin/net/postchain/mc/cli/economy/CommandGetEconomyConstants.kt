package net.postchain.mc.cli.economy

import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.getEconomyConstants
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatECV45.getEconomyConstantsV45

class CommandGetEconomyConstants : ECBaseCommand(
        name = "get-constants",
        help = "Get economy chain constants",
        printHelpOnEmptyArgs = false,
) {
    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        if (ecVersion.version < ECONOMY_CHAIN_STAKING_REQ_NODE_BASED_VERSION) {
            val economyConstants = economyChainClient.getEconomyConstantsV45()
            echo(pmcTable {
                captionTop("Economy chain constants")
                body {
                    row("Min lease time weeks", economyConstants.minLeaseTimeWeeks)
                    row("Max lease time weeks", economyConstants.maxLeaseTimeWeeks)
                    row("CHR per USD", economyConstants.chrPerUsd)
                    row("Total cost system providers in USD", formatUsd(economyConstants.totalCostSystemProviders, ecVersion.version))
                    row("System provider fee share", economyConstants.systemProviderFeeShare)
                    row("Staking reward rate", economyConstants.stakingRewardRate)
                    row("Staking reward fee share", economyConstants.stakingRewardFeeShare)
                    row("Chromia foundation fee share", economyConstants.chromiaFoundationFeeShare)
                    row("Resource pool margin fee share", economyConstants.resourcePoolMarginFeeShare)
                    row("System provider risk share", economyConstants.systemProviderRiskShare)
                    row("Staking requirements enabled", economyConstants.stakingRequirementsEnabled)
                    row("Staking requirements stop payout days", economyConstants.stakingRequirementStopPayoutDays)
                    row("Staking requirements system provider own",  formatChr(economyConstants.stakingRequirementSystemProviderOwnStakeChr, ecVersion.version))
                    row("Staking requirements system provider total", formatChr(economyConstants.stakingRequirementSystemProviderTotalStakeChr, ecVersion.version))
                    row("Staking requirements dapp provider own", formatChr(economyConstants.stakingRequirementDappProviderOwnStakeChr, ecVersion.version))
                    row("Staking requirements dapp provider total", formatChr(economyConstants.stakingRequirementDappProviderTotalStakeChr, ecVersion.version))
                }
            })
        } else {
            val economyConstants = economyChainClient.getEconomyConstants()
            echo(pmcTable {
                captionTop("Economy chain constants")
                body {
                    row("Min lease time weeks", economyConstants.minLeaseTimeWeeks)
                    row("Max lease time weeks", economyConstants.maxLeaseTimeWeeks)
                    row("CHR per USD", economyConstants.chrPerUsd)
                    row("Total cost system providers in USD", formatUsd(economyConstants.totalCostSystemProviders, ecVersion.version))
                    row("System provider fee share", economyConstants.systemProviderFeeShare)
                    row("Staking reward rate", economyConstants.stakingRewardRate)
                    row("Staking reward fee share", economyConstants.stakingRewardFeeShare)
                    row("Chromia foundation fee share", economyConstants.chromiaFoundationFeeShare)
                    row("Resource pool margin fee share", economyConstants.resourcePoolMarginFeeShare)
                    row("System provider risk share", economyConstants.systemProviderRiskShare)
                    row("Staking requirements enabled", economyConstants.stakingRequirementsEnabled)
                    row("Staking requirements stop payout days", economyConstants.stakingRequirementStopPayoutDays)
                    row("Staking requirements system node own", formatChr(economyConstants.stakingRequirementSystemNodeOwnStakeChr, ecVersion.version))
                    row("Staking requirements system node total", formatChr(economyConstants.stakingRequirementSystemNodeTotalStakeChr, ecVersion.version))
                    row("Staking requirements dapp node own", formatChr(economyConstants.stakingRequirementDappNodeOwnStakeChr, ecVersion.version))
                    row("Staking requirements dapp node total", formatChr(economyConstants.stakingRequirementDappNodeTotalStakeChr, ecVersion.version))
                }
            })
        }
    }
}