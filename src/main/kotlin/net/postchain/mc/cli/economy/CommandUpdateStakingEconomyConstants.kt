package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.updateStakingRequirementsEconomyConstantsOperation
import net.postchain.mc.cli.base.printResult

class CommandUpdateStakingEconomyConstants : ECBaseCommand(
        name = "update-staking-constants",
        help = "Update staking economy chain constants which is required to be meet in order for providers to receive rewards.",
        requiresECVersion = ECONOMY_CHAIN_STAKING_REQUIREMENTS_VERSION
) {

    private val stakingRequirementsEnabled by option("--staking-requirements-enabled", help = "Enable or disable the staking requirement check on reward pay out").boolean()
    private val stakingRequirementsStopPayoutDays by option("--staking-requirements-stop-payout-days", help = "Number of days a provider can fail to meet the staking requirements no longer receiving reward payouts").long()
    private val stakingRequirementsSystemProviderOwnStakeChr by option("--staking-requirements-sp-own", help = "Required provider staking amount in CHR for system providers").long()
    private val stakingRequirementsSystemProviderTotalStakeChr by option("--staking-requirements-sp-total", help = "Required total staking amount in CHR for system providers").long()
    private val stakingRequirementsDappProviderOwnStakeChr by option("--staking-requirements-dp-own", help = "Required provider staking amount in CHR for dapp providers").long()
    private val stakingRequirementsDappProviderTotalStakeChr by option("--staking-requirements-dp-total", help = "Required total staking amount in CHR for dapp providers").long()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        if (
                listOfNotNull(
                        stakingRequirementsEnabled,
                        stakingRequirementsStopPayoutDays,
                        stakingRequirementsSystemProviderOwnStakeChr,
                        stakingRequirementsSystemProviderTotalStakeChr,
                        stakingRequirementsDappProviderOwnStakeChr,
                        stakingRequirementsDappProviderTotalStakeChr,
                ).isEmpty()
        ) {
            throw CliktError("No variable provided")
        }

        economyChainClient.transactionBuilder()
                .updateStakingRequirementsEconomyConstantsOperation(
                        stakingRequirementsEnabled,
                        stakingRequirementsStopPayoutDays,
                        stakingRequirementsSystemProviderOwnStakeChr,
                        stakingRequirementsSystemProviderTotalStakeChr,
                        stakingRequirementsDappProviderOwnStakeChr,
                        stakingRequirementsDappProviderTotalStakeChr,
                )
                .postAwaitConfirmation()
                .printResult(
                        "Economy staking constants updated.",
                        "Failed to update staking economy constants"
                )
    }
}
