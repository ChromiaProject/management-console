package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.proposeSystemProviderEconomyConstantsOperation
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.printResult

class CommandUpdateSystemProviderEconomyConstants : ECBaseCommand(
        name = "update-system-provider-constants",
        help = "Update system provider economy chain constants"
) {

    private val totalCostSystemProviders by option("--total-cost-system-providers", help = "Total cost system providers in USD")
    private val systemProviderFeeShare by option("--system-provider-fee-share", help = "System provider fee share")
    private val systemProviderRiskShare by option("--system-provider-risk-share", help = "System provider risk share")

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        if (totalCostSystemProviders == null && systemProviderFeeShare == null && systemProviderRiskShare == null) {
            throw CliktError("No variable provided")
        }

        transactionBuilder()
                .proposeSystemProviderEconomyConstantsOperation(
                        clientProviderPubkey,
                        totalCostSystemProviders?.toLong()?.times(UNITS_PER_USD),
                        systemProviderFeeShare?.toBigDecimal(),
                        systemProviderRiskShare?.toBigDecimal(),
                )
                .postOrSave()
                .printResult(
                        "Proposal for updating system provider economy constants is created and awaits approval.",
                        "Failed to create system provider economy constants update proposal"
                )
    }
}