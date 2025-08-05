package net.postchain.mc.network

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.token_chain_in_directory_chain.initEvmEventReceiverTokenChainOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.BlockchainConfig

class CommandInitEvmEventReceiverTokenChain : DCBaseCommand(
        name = "initialize-evm-event-receiver-token-chain",
        help = "Create and initialize EVM event receiver token chain",
        printHelpOnEmptyArgs = true
) {
    private val eventReceiverConfig by option(
            "-erc",
            "--event-receiver-config",
            help = "Configuration file for EVM event receiver token chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    override fun runDC() {
        if (dcVersion < 75) {
            throw CliktError("EVM event receiver token chain requires directory chain version 75, found version $dcVersion")
        }

        eventReceiverConfig?.let {
            val config = BlockchainConfig.readFromFile(it)
            val compressedConfig = BlockchainConfigurationCompressor.compress(client, config.gtv, dcVersion)
            client.transactionBuilder()
                    .initEvmEventReceiverTokenChainOperation(clientProviderPubkey, GtvEncoder.encodeGtv(compressedConfig))
                    .postAwaitConfirmation()
                    .printResult(
                            "EVM event receiver token chain was created",
                            "Failed to create EVM event receiver token chain",
                            printOnSuccess = true
                    )
        }
    }
}
