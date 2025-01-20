package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.updateEconomyConstantsOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.scheduleAt
import net.postchain.mc.compatibility.ApiCompatECV51.updateEconomyConstantsOperationECV51

class CommandUpdateEconomyConstants : ECBaseCommand(
    name = "update-constants",
    help = "Update economy chain constants"
) {
    private val minLeaseTimeWeeks by option("--min-lease-time", help = "Number of weeks as minimum for a lease").long()
    private val maxLeaseTimeWeeks by option("--max-lease-time", help = "Number of weeks as maximum for a lease").long()
    private val stakingRewardRate by option("--staking-reward-rate", help = "Staking reward rate")
    private val stakingRewardFeeShare by option("--staking-reward-fee-share", help = "Staking reward fee share")
    private val chromiaFoundationFeeShare by option("--chromia-foundation-fee-share", help = "Chromia foundation fee share")
    private val resourcePoolMarginFeeShare by option("--resource-pool-margin-fee-share", help = "Resource pool margin fee share")
    private val dappProviderRiskShare by option("--dapp-provider-risk-share", help = "Dapp provider risk share")
    private val scheduleAt by scheduleAt()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        if (listOfNotNull(
                        minLeaseTimeWeeks,
                        maxLeaseTimeWeeks,
                        stakingRewardRate,
                        stakingRewardFeeShare,
                        chromiaFoundationFeeShare,
                        resourcePoolMarginFeeShare,
                        dappProviderRiskShare,
        ).isEmpty()) {
            throw CliktError("No variable provided")
        }

        if (ecVersion.version < ECONOMY_CHAIN_SCHEDULED_PROPOSAL_VERSION) {
            economyChainClient.transactionBuilder()
                    .updateEconomyConstantsOperationECV51(minLeaseTimeWeeks, maxLeaseTimeWeeks, stakingRewardRate?.toBigDecimal(),
                            stakingRewardFeeShare?.toBigDecimal(), chromiaFoundationFeeShare?.toBigDecimal(),
                            resourcePoolMarginFeeShare?.toBigDecimal(), dappProviderRiskShare?.toBigDecimal()
                    )
        } else {
            economyChainClient.transactionBuilder()
                    .updateEconomyConstantsOperation(minLeaseTimeWeeks, maxLeaseTimeWeeks, stakingRewardRate?.toBigDecimal(),
                            stakingRewardFeeShare?.toBigDecimal(), chromiaFoundationFeeShare?.toBigDecimal(),
                            resourcePoolMarginFeeShare?.toBigDecimal(), dappProviderRiskShare?.toBigDecimal(), scheduleAt
                    )
        }
                .postAwaitConfirmation()
                .printResult(
                        "Proposal for updating economy constants is created and awaits approval.",
                        "Failed to create economy constants update proposal"
                )
    }
}