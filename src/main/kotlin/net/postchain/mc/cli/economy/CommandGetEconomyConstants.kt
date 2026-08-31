package net.postchain.mc.cli.economy

import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.getEconomyConstants
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.ECONOMY_CHAIN_DYNAMIC_STAKING_REWARD_SHARE_VERSION
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatECV63.getEconomyConstantsECV63

class CommandGetEconomyConstants : ECBaseCommand(
        name = "get-constants",
        help = "Get economy chain constants",
        printHelpOnEmptyArgs = false,
) {
    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        if (ecVersion.version < ECONOMY_CHAIN_DYNAMIC_STAKING_REWARD_SHARE_VERSION) {
            val economyConstants = economyChainClient.getEconomyConstantsECV63()
            echo(pmcTable {
                captionTop("Economy chain constants")
                body {
                    row("Min lease time weeks", economyConstants.minLeaseTimeWeeks)
                    row("Max lease time weeks", economyConstants.maxLeaseTimeWeeks)
                    row("CHR per USD", economyConstants.chrPerUsd)
                    row("Total cost system providers in USD", formatUsd(economyConstants.totalCostSystemProviders))
                    row("System provider fee share", economyConstants.systemProviderFeeShare)
                    row("Staking reward rate", economyConstants.stakingRewardRate)
                    row("Staking reward fee share", economyConstants.stakingRewardFeeShare)
                    row("Chromia foundation fee share", economyConstants.chromiaFoundationFeeShare)
                    row("Resource pool margin fee share", economyConstants.resourcePoolMarginFeeShare)
                    row("System provider risk share", economyConstants.systemProviderRiskShare)
                    row("Dapp provider risk share", economyConstants.dappProviderRiskShare)
                    row("Staking requirements enabled", economyConstants.stakingRequirementsEnabled)
                    row("Staking requirements stop payout days", economyConstants.stakingRequirementStopPayoutDays)
                    row("Staking requirements system node own", formatChr(economyConstants.stakingRequirementSystemNodeOwnStakeChr))
                    row("Staking requirements system node total", formatChr(economyConstants.stakingRequirementSystemNodeTotalStakeChr))
                    row("Staking requirements dapp node own", formatChr(economyConstants.stakingRequirementDappNodeOwnStakeChr))
                    row("Staking requirements dapp node total", formatChr(economyConstants.stakingRequirementDappNodeTotalStakeChr))
                    row("Bridge lease add min balance in USD", economyConstants.bridgeLeaseAddMinBalanceUsd)
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
                    row("Total cost system providers in USD", formatUsd(economyConstants.totalCostSystemProviders))
                    row("System provider fee share", economyConstants.systemProviderFeeShare)
                    row("Staking reward rate", economyConstants.stakingRewardRate)
                    row("Chromia foundation fee share", economyConstants.chromiaFoundationFeeShare)
                    row("Resource pool margin fee share", economyConstants.resourcePoolMarginFeeShare)
                    row("System provider risk share", economyConstants.systemProviderRiskShare)
                    row("Dapp provider risk share", economyConstants.dappProviderRiskShare)
                    row("Staking requirements enabled", economyConstants.stakingRequirementsEnabled)
                    row("Staking requirements stop payout days", economyConstants.stakingRequirementStopPayoutDays)
                    row("Staking requirements system node own", formatChr(economyConstants.stakingRequirementSystemNodeOwnStakeChr))
                    row("Staking requirements system node total", formatChr(economyConstants.stakingRequirementSystemNodeTotalStakeChr))
                    row("Staking requirements dapp node own", formatChr(economyConstants.stakingRequirementDappNodeOwnStakeChr))
                    row("Staking requirements dapp node total", formatChr(economyConstants.stakingRequirementDappNodeTotalStakeChr))
                    row("Bridge lease add min balance in USD", economyConstants.bridgeLeaseAddMinBalanceUsd)
                }
            })
        }
    }
}