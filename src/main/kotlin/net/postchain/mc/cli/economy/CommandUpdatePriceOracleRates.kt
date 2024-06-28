package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.PendingPriceOracleRateData
import net.postchain.economy.economy_chain.proposePriceOracleRateOperation
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtv.parse.GtvParser
import net.postchain.mc.cli.base.printResult

class CommandUpdatePriceOracleRates : ECBaseCommand(
        name = "update-price-oracle-rates",
        help = """
            Create a proposal to update price oracle token prices. Multiple tokens can be added/updated in each proposal.
            
            Example:
            ```
            pmc economy update-price-oracle-rates --token-rates '["symbol": "CHR", "name": "Chromia", "price": "12.34"]' --token-rates '["symbol": "tCHR", "name": "Test Chromia", "price": "34.56"]'
            ```
    """.trimIndent(),
        requiresECVersion = ECONOMY_CHAIN_PRICE_ORACLE_RATE_PROPOSAL_VERSION,
        printHelpOnEmptyArgs = true
) {

    private val tokenRates by option(
            help = "GTV formatted token with rate and possible name. Each proposal can contain multiple token updates.",
    ).convert {
        val pi = GtvParser.parse(it).asDict().toMutableMap()
        
        GtvObjectMapper.fromGtv(gtv(pi), PendingPriceOracleRateData::class)
    }.multiple(required = true)

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        if (tokenRates.isEmpty()) {
            throw CliktError("No tokens provided")
        }

        when {
            else -> {
                economyChainClient.transactionBuilder()
                        .proposePriceOracleRateOperation(tokenRates)
            }
        }
                .postAwaitConfirmation()
                .printResult(
                        "Proposal for updating price oracle rates is created and awaits approval.",
                        "Failed to create price oracle rate proposal"
                )
    }
}