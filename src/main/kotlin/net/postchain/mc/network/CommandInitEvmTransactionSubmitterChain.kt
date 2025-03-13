package net.postchain.mc.network

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.evm_transaction_submitter.initEvmTransactionSubmitterChainOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.BlockchainConfig

class CommandInitEvmTransactionSubmitterChain : DCBaseCommand(
        name = "initialize-evm-transaction-submitter-chain",
        help = "Create and initialize EVM transaction submitter chain",
        printHelpOnEmptyArgs = true
) {
    private val transactionSubmitterConfig by option(
            "-tsc",
            "--transaction-submitter-config",
            help = "Configuration file for EVM transaction submitter chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    override fun runDC() {
        if (dcVersion < 36) {
            echo("EVM transaction submitter chain requires directory chain version 36, found version $dcVersion")
            return
        }

        transactionSubmitterConfig?.let {
            val transactionSubmitterChainConfigData = BlockchainConfig.readFromFile(it)
            val compressedTransactionSubmitterChainConfig = BlockchainConfigurationCompressor.compress(client, transactionSubmitterChainConfigData.gtv, dcVersion)
            client.transactionBuilder()
                    .initEvmTransactionSubmitterChainOperation(clientProviderPubkey, GtvEncoder.encodeGtv(compressedTransactionSubmitterChainConfig))
                    .postAwaitConfirmation()
                    .printResult(
                            "EVM transaction submitter chain was created",
                            "Failed to create EVM transaction submitter chain",
                            printOnSuccess = true
                    )
        }
    }
}
