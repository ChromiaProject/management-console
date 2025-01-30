package net.postchain.mc.network

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.economy_chain_in_directory_chain.initEconomyChainOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.economy.DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.pmcConfigOption

class CommandInitEconomyChain : PmcCommand(
        name = "initialize-economy-chain",
        help = "Create and initialize economy chain. Please run the command without supplying the configuration to retry a failed initialization.",
        printHelpOnEmptyArgs = false
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val economyChainConfig by option(
            "-ecc",
            "--economy-chain-config",
            help = "Configuration file for economy chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    override fun run() {
        val version = Version(client).version
        if (version < DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION) {
            echo("Economy chain requires directory chain version $DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION, found version $version")
            return
        }

        economyChainConfig?.let {
            val economyChainConfigData = BlockchainConfig.readFromFile(it)
            val compressedEconomyChainConfig = BlockchainConfigurationCompressor.compress(client, economyChainConfigData.gtv, version)
            client.transactionBuilder()
                    .initEconomyChainOperation(client.pubkey, GtvEncoder.encodeGtv(compressedEconomyChainConfig))
                    .postAwaitConfirmation()
                    .printResult(
                            "Economy chain was created",
                            "Failed to create Economy chain",
                            printOnSuccess = true
                    )
        }

        initEconomyChain(client, config.chromiaClient)
    }
}
