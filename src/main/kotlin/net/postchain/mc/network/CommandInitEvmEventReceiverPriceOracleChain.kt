package net.postchain.mc.network

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.evm_event_receiver_price_oracle.initEvmEventReceiverPriceOracleChainOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.BlockchainConfig

class CommandInitEvmEventReceiverPriceOracleChain : DCBaseCommand(
        name = "initialize-evm-event-receiver-price-oracle-chain",
        help = "Create and initialize EVM event receiver price oracle chain",
        printHelpOnEmptyArgs = true
) {
    private val eventReceiverConfig by option(
            "-erc",
            "--event-receiver-config",
            help = "Configuration file for EVM event receiver price oracle chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    override fun runDC() {
        if (dcVersion < 51) {
            throw CliktError("EVM event receiver price oracle chain requires directory chain version 51, found version $dcVersion")
        }

        eventReceiverConfig?.let {
            val config = BlockchainConfig.readFromFile(it)
            val compressedConfig = BlockchainConfigurationCompressor.compress(client, config.gtv, dcVersion)
            transactionBuilder()
                    .initEvmEventReceiverPriceOracleChainOperation(clientProviderPubkey, GtvEncoder.encodeGtv(compressedConfig))
                    .postAwaitConfirmation(txListener())
                    .printResult(
                            "EVM event receiver price oracle chain was created",
                            "Failed to create EVM event receiver price oracle chain",
                            printOnSuccess = true
                    )
        }
    }
}
