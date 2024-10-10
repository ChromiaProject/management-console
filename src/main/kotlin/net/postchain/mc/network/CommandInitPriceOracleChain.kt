package net.postchain.mc.network

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.price_oracle.initPriceOracleChainOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.pmcConfigOption

class CommandInitPriceOracleChain : PmcCommand(
        name = "initialize-price-oracle-chain",
        help = "Create and initialize Price oracle chain"
) {
    override val printHelpOnEmptyArgs: Boolean
        get() = true

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val priceOracleConfig by option(
            "-poc",
            "--price-oracle-config",
            help = "Configuration file for Price oracle chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    override fun run() {
        val version = Version(client).version
        if (version < 37) {
            echo("Price oracle chain requires directory chain version 37, found version $version")
            return
        }

        priceOracleConfig?.let {
            val priceOracleChainConfigData = BlockchainConfig.readFromFile(it)
            val compressedPriceOracleChainConfig = BlockchainConfigurationCompressor.compress(client, priceOracleChainConfigData.gtv, version)
            client.transactionBuilder()
                    .initPriceOracleChainOperation(client.pubkey, GtvEncoder.encodeGtv(compressedPriceOracleChainConfig))
                    .postAwaitConfirmation()
                    .printResult(
                            "Price oracle chain was created",
                            "Failed to create Price oracle chain",
                            printOnSuccess = true
                    )
        }
    }
}
