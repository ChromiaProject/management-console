package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.updateTagOperation
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.optionalPriceOption
import net.postchain.mc.compatibility.ApiCompatECV66.updateTagOperationECV66

class CommandUpdateTag : ECBaseCommand(
        name = "update-tag",
        help = "Update an existing tag"
) {
    private val name by nameOption("Name of the tag").required()

    private val scuPrice by optionalPriceOption("-scup", "--scu-price", help = "SCU price in USD per day.")
    private val extraStoragePrice by optionalPriceOption("-esp", "--extra-storage-price", help = "Extra storage price in USD per day.")
    private val extraComputeRequestPrice by optionalPriceOption("-ecrp", "--extra-compute-request-price", help = "Extra compute request price in USD.")

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        if (listOfNotNull(scuPrice, extraStoragePrice, extraComputeRequestPrice).isEmpty()) {
            throw CliktError("Must specify either SCU price, extra storage price or extra compute request price")
        }

        if (extraComputeRequestPrice != null && ecVersion.version < ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION) {
            throw CliktError("This version of Economy chain does not support extra compute request price on tags")
        }

        when {
            ecVersion.version < ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION -> {
                transactionBuilder()
                        .updateTagOperationECV66(
                                clientProviderPubkey, name,
                                scuPrice?.times(UNITS_PER_USD.toBigDecimal())?.toLong(),
                                extraStoragePrice?.times(UNITS_PER_USD.toBigDecimal())?.toLong(),
                        )
            }

            else -> {
                transactionBuilder()
                        .updateTagOperation(
                                clientProviderPubkey, name,
                                scuPrice?.times(UNITS_PER_USD.toBigDecimal())?.toLong(),
                                extraStoragePrice?.times(UNITS_PER_USD.toBigDecimal())?.toLong(),
                                extraComputeRequestPrice?.times(UNITS_PER_USD.toBigDecimal())?.toLong(),
                        )
            }
        }
                .postOrSave()
                .printResult(
                        "Proposal for updating tag $name is created",
                        "Failed to update tag"
                )
    }
}