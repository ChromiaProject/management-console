package net.postchain.mc.network

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.economy_chain_in_directory_chain.initEconomyChainOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.BlockchainConfig

class CommandInitEconomyChain : DCBaseCommand(
        name = "initialize-economy-chain",
        help = "Create and initialize economy chain. Please run the command without supplying the configuration to retry a failed initialization.",
        printHelpOnEmptyArgs = false
) {
    private val economyChainConfig by option(
            "-ecc",
            "--economy-chain-config",
            help = "Configuration file for economy chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    override fun runDC() {
        if (dcVersion < DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION) {
            CliktError("Economy chain requires directory chain version $DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION, found version $dcVersion")
        }

        economyChainConfig?.let {
            val economyChainConfigData = BlockchainConfig.readFromFile(it)
            val compressedEconomyChainConfig = BlockchainConfigurationCompressor.compress(client, economyChainConfigData.gtv, dcVersion)
            transactionBuilder()
                    .initEconomyChainOperation(clientProviderPubkey, GtvEncoder.encodeGtv(compressedEconomyChainConfig))
                    .postOrSave()
                    .printResult(
                            "Economy chain was created",
                            "Failed to create Economy chain",
                            printOnSuccess = true
                    )
        }

        initEconomyChain(client, config.chromiaClient)
    }
}
