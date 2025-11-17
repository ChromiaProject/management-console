package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.createTagOperation
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.compatibility.ApiCompatECV57.createTagOperationV57
import java.math.BigDecimal

class CommandAddTag : ECBaseCommand(
        name = "add-tag",
        help = "Add a new tag"
) {
    private val name by nameOption("Name of the new tag").required().validate(entityNameValidator())

    private val scuPrice by option("-scup", "--scu-price", help = "SCU price in USD per day.")
            .convert { BigDecimal(it) }
            .required()
            .validate {
                if (it <= BigDecimal.ZERO) {
                    throw CliktError("Tag must have a positive SCU price")
                }
            }

    private val extraStoragePrice by option("-esp", "--extra-storage-price", help = "Extra storage price in USD per day.")
            .convert { BigDecimal(it) }
            .required()
            .validate {
                if (it <= BigDecimal.ZERO) {
                    throw CliktError("Tag must have a positive extra storage price")
                }
            }

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        when {

            ecVersion.version < ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION -> {

                if (scuPrice.stripTrailingZeros().scale() != 0 || extraStoragePrice.stripTrailingZeros().scale() != 0) {
                    throw CliktError("This version of Economy chain only support price in dollar (no minor/decimal units)")
                }

                transactionBuilder()
                        .createTagOperationV57(name, scuPrice.toLong(), extraStoragePrice.toLong())
            }

            ecVersion.version < ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION -> {
                transactionBuilder()
                        .createTagOperationV57(name, scuPrice.times(UNITS_PER_USD.toBigDecimal()).toLong(), extraStoragePrice.times(UNITS_PER_USD.toBigDecimal()).toLong())
            }

            else -> {
                transactionBuilder()
                        .createTagOperation(clientProviderPubkey, name, scuPrice.times(UNITS_PER_USD.toBigDecimal()).toLong(), extraStoragePrice.times(UNITS_PER_USD.toBigDecimal()).toLong())
            }
        }
                .postOrSave()
                .printResult(
                        "Proposal for creating tag $name is created",
                        "Failed to create tag"
                )
    }
}