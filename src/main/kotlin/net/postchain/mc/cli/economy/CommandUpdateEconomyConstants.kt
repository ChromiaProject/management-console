package net.postchain.mc.cli.economy

import com.chromia.directory1.economy_chain.updateEconomyConstantsOperation
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.client.core.PostchainClient
import net.postchain.mc.cli.base.printResult

class CommandUpdateEconomyConstants : ECBaseCommand(
    name = "update-constants",
    help = "Update economy chain constants"
) {

    private val minLeaseTimeWeeks by option("--min-lease-time", help = "Number of weeks as minimum for a lease").long()
    private val maxLeaseTimeWeeks by option("--max-lease-time", help = "Number of weeks as maximum for a lease").long()
    private val stakingRewardFeeShare by option("--staking-reward-fee-share", help = "Staking reward fee share")
    private val chromiaFoundationFeeShare by option("--chromia-foundation-fee-share", help = "Chromia foundation fee share")
    private val resourcePoolMarginFeeShare by option("--resource-pool-margin-fee-share", help = "Resource pool margin fee share")
    private val dappProviderRiskShare by option("--dapp-provider-risk-share", help = "Dapp provider risk share")

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        if (minLeaseTimeWeeks == null && maxLeaseTimeWeeks == null && stakingRewardFeeShare == null &&
            chromiaFoundationFeeShare == null && resourcePoolMarginFeeShare == null && dappProviderRiskShare == null
        ) {
            throw CliktError("No variable provided")
        }

        economyChainClient.transactionBuilder()
            .updateEconomyConstantsOperation(minLeaseTimeWeeks, maxLeaseTimeWeeks, stakingRewardFeeShare?.toBigDecimal(),
                chromiaFoundationFeeShare?.toBigDecimal(), resourcePoolMarginFeeShare?.toBigDecimal(), dappProviderRiskShare?.toBigDecimal())
            .postAwaitConfirmation()
            .printResult(
                "Proposal for updating economy constants is created and awaits approval.",
                "Failed to create economy constants update proposal"
            )
    }
}