package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.updateSystemProviderEconomyConstantsOperation
import net.postchain.mc.cli.base.printResult

class CommandUpdateSystemProviderEconomyConstants : ECBaseCommand(
        name = "update-system-provider-constants",
        help = "Update system provider economy chain constants"
) {

    private val totalCostSystemProviders by option("--total-cost-system-providers", help = "Total cost system providers")
    private val systemProviderFeeShare by option("--system-provider-fee-share", help = "System provider fee share")
    private val systemProviderRiskShare by option("--system-provider-risk-share", help = "System provider risk share")

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        if (totalCostSystemProviders == null && systemProviderFeeShare == null && systemProviderRiskShare == null) {
            throw CliktError("No variable provided")
        }

        economyChainClient.transactionBuilder()
                .updateSystemProviderEconomyConstantsOperation(
                        totalCostSystemProviders?.toLong(),
                        systemProviderFeeShare?.toBigDecimal(),
                        systemProviderRiskShare?.toBigDecimal(),
                )
                .postAwaitConfirmation()
                .printResult(
                        "Economy system provider constants updated.",
                        "Failed to update system provider economy constants"
                )
    }
}