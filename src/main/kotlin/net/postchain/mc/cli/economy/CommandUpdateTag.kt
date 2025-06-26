package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.updateTagOperation
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.compatibility.ApiCompatECV57.updateTagOperationV57
import java.math.BigDecimal

class CommandUpdateTag : ECBaseCommand(
        name = "update-tag",
        help = "Update an existing tag"
) {
    private val name by nameOption("Name of the tag").required()

    private val scuPrice by option("-scup", "--scu-price", help = "Updated SCU price in USD per day")
            .convert { BigDecimal(it) }
            .validate {
                if (it <= BigDecimal.ZERO) {
                    throw CliktError("Tag must have a positive SCU price")
                }
            }

    private val extraStoragePrice by option("-esp", "--extra-storage-price", help = "Updated extra storage price in USD per day")
            .convert { BigDecimal(it) }
            .validate {
                if (it <= BigDecimal.ZERO) {
                    throw CliktError("Tag must have a positive extra storage price")
                }
            }

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        if (scuPrice == null && extraStoragePrice == null) {
            throw CliktError("Must specify either updated SCU price or extra storage price")
        }

        when {
            ecVersion.version < ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION -> {

                if ((scuPrice != null && scuPrice!!.stripTrailingZeros().scale() != 0) || (extraStoragePrice != null && extraStoragePrice!!.stripTrailingZeros().scale() != 0)) {
                    throw CliktError("This version of Economy chain only support price in dollar (no minor/decimal units)")
                }

                economyChainClient.transactionBuilder()
                        .updateTagOperationV57(name, scuPrice?.toLong(), extraStoragePrice?.toLong())
            }

            ecVersion.version < ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION -> {
                economyChainClient.transactionBuilder()
                        .updateTagOperationV57(name, scuPrice?.times(UNITS_PER_USD.toBigDecimal())?.toLong(), extraStoragePrice?.times(UNITS_PER_USD.toBigDecimal())?.toLong())
            }
            else -> {
                economyChainClient.transactionBuilder()
                        .updateTagOperation(
                                clientProviderPubkey, name,
                                scuPrice?.times(UNITS_PER_USD.toBigDecimal())?.toLong(),
                                extraStoragePrice?.times(UNITS_PER_USD.toBigDecimal())?.toLong(),
                        )
            }
        }
                .postAwaitConfirmation()
                .printResult(
                        "Proposal for updating tag $name is created",
                        "Failed to update tag"
                )
    }
}