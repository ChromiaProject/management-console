package net.postchain.mc.network

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.evm_event_receiver_price_oracle.initEvmEventReceiverPriceOracleChainOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.pmcConfigOption

class CommandInitEvmEventReceiverPriceOracleChain : CliktCommand(
        name = "initialize-evm-event-receiver-price-oracle-chain",
        help = "Create and initialize EVM event receiver price oracle chain",
        printHelpOnEmptyArgs = true
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val eventReceiverConfig by option(
            "-erc",
            "--event-receiver-config",
            help = "Configuration file for EVM event receiver price oracle chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    override fun run() {
        val version = Version(client).version
        if (version < 51) {
            echo("EVM event receiver price oracle chain requires directory chain version 51, found version $version")
            return
        }

        eventReceiverConfig?.let {
            val config = BlockchainConfig.readFromFile(it)
            val compressedConfig = BlockchainConfigurationCompressor.compress(client, config.gtv, version)
            client.transactionBuilder()
                    .initEvmEventReceiverPriceOracleChainOperation(client.pubkey, GtvEncoder.encodeGtv(compressedConfig))
                    .postAwaitConfirmation()
                    .printResult(
                            "EVM event receiver price oracle chain was created",
                            "Failed to create EVM event receiver price oracle chain",
                            printOnSuccess = true
                    )
        }
    }
}
