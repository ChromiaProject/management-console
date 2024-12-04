package net.postchain.mc.network

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.token_chain_in_directory_chain.initTokenChainOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.pmcConfigOption

class CommandInitTokenChain : PmcCommand(
        name = "initialize-token-chain",
        help = "Create and initialize token chain. Please run the command without supplying the configuration to retry a failed initialization.",
        printHelpOnEmptyArgs = true
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val chainConfig by option(
            "-tcc",
            "--token-chain-config",
            help = "Configuration file for token chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    override fun run() {
        val version = Version(client).version
        if (version < 75) {
            echo("Token chain requires directory chain version 75, found version $version")
            return
        }

        chainConfig?.let {
            val chainConfigData = BlockchainConfig.readFromFile(it)
            val compressedChainConfig = BlockchainConfigurationCompressor.compress(client, chainConfigData.gtv, version)
            client.transactionBuilder()
                    .initTokenChainOperation(client.pubkey, GtvEncoder.encodeGtv(compressedChainConfig))
                    .postAwaitConfirmation()
                    .printResult(
                            "Token chain was created",
                            "Failed to create Token chain",
                            printOnSuccess = true
                    )
        }

        initTokenChain(client, config.config)
    }
}
