package net.postchain.mc.network

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.token_chain_in_directory_chain.initTokenChainOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.BlockchainConfig

class CommandInitTokenChain : DCBaseCommand(
        name = "initialize-token-chain",
        help = """
            Create and initialize token chain. Please run the command without supplying the configuration to only run initialize operation.
            In case deployer key is not the same as the initializer key on token chain, run the command with '--only-deploy' flag first.
            """,
        printHelpOnEmptyArgs = false
) {
    private val chainConfig by option(
            "-tcc",
            "--token-chain-config",
            help = "Configuration file for token chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    private val onlyDeploy by option("--only-deploy", help = "Don't call initialize operation on token chain")
            .flag(default = false)

    override fun runDC() {
        if (dcVersion < 75) {
            echo("Token chain requires directory chain version 75, found version $dcVersion")
            return
        }

        chainConfig?.let {
            val chainConfigData = BlockchainConfig.readFromFile(it)
            val compressedChainConfig = BlockchainConfigurationCompressor.compress(client, chainConfigData.gtv, dcVersion)
            transactionBuilder()
                    .initTokenChainOperation(clientProviderPubkey, GtvEncoder.encodeGtv(compressedChainConfig))
                    .postOrSave()
                    .printResult(
                            "Token chain was created",
                            "Failed to create Token chain",
                            printOnSuccess = true
                    )
        }

        if (!onlyDeploy) {
            initTokenChain(client, config.chromiaClient)
        }
    }
}
