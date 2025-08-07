package net.postchain.mc.network

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.price_oracle.initPriceOracleChainOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.BlockchainConfig

class CommandInitPriceOracleChain : DCBaseCommand(
        name = "initialize-price-oracle-chain",
        help = "Create and initialize Price oracle chain",
        printHelpOnEmptyArgs = true
) {
    private val priceOracleConfig by option(
            "-poc",
            "--price-oracle-config",
            help = "Configuration file for Price oracle chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    override fun runDC() {
        if (dcVersion < 37) {
            throw CliktError("Price oracle chain requires directory chain version 37, found version $dcVersion")
        }

        priceOracleConfig?.let {
            val priceOracleChainConfigData = BlockchainConfig.readFromFile(it)
            val compressedPriceOracleChainConfig = BlockchainConfigurationCompressor.compress(client, priceOracleChainConfigData.gtv, dcVersion)
            client.transactionBuilder()
                    .initPriceOracleChainOperation(clientProviderPubkey, GtvEncoder.encodeGtv(compressedPriceOracleChainConfig))
                    .postAwaitConfirmation(txListener())
                    .printResult(
                            "Price oracle chain was created",
                            "Failed to create Price oracle chain",
                            printOnSuccess = true
                    )
        }
    }
}
