package net.postchain.mc.network

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.common.init.initOperation
import net.postchain.chain0.economy_chain_in_directory_chain.initEconomyChainOperation
import net.postchain.chain0.token_chain_in_directory_chain.initTokenChainOperation
import net.postchain.client.core.PostchainClient
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.pmcConfigOption
import java.io.File

class CommandInit : PmcCommand(
        name = "initialize",
        help = "Create system cluster with naked system container for the directory blockchain. Module argument initial_provider becomes first member of SYSTEM_P voter set."
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val systemAnchoringConfig by option(
            "-sac",
            "--system-anchoring-config",
            help = "Configuration file for system anchoring chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    private val clusterAnchoringConfig by option(
            "-cac",
            "--cluster-anchoring-config",
            help = "Configuration file for cluster anchoring chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    private val economyChainConfig by option(
            "-ecc",
            "--economy-chain-config",
            help = "Configuration file for economy chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    private val tokenChainConfig by option(
            "-tcc",
            "--token-chain-config",
            help = "Configuration file for token chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    override fun run() {
        if (systemAnchoringConfig != null && clusterAnchoringConfig == null) {
            echo("System anchoring requires cluster anchoring. Please specify a cluster anchoring configuration.")
            return
        }

        val version = Version(client).version
        val systemAnchoringConfigData = systemAnchoringConfig?.let { readAndCompressConfigurationFromFile(client, it, version) }
        val clusterAnchoringConfigData = clusterAnchoringConfig?.let { readAndCompressConfigurationFromFile(client, it, version) }

        val economyChainConfigData = economyChainConfig?.let { readAndCompressConfigurationFromFile(client, it, version) }
        if (version < 30 && economyChainConfigData != null) {
            echo("Economy chain requires directory chain version 30, found version $version")
            return
        }

        val tokenChainConfigData = tokenChainConfig?.let { readAndCompressConfigurationFromFile(client, it, version) }
        if (version < 75 && tokenChainConfigData != null) {
            echo("Token chain requires directory chain version 75, found version $version")
            return
        }

        client.transactionBuilder()
                .initOperation(systemAnchoringConfigData, clusterAnchoringConfigData)
                .apply {
                    economyChainConfigData?.let {
                        initEconomyChainOperation(client.pubkey, it)
                    }
                    tokenChainConfigData?.let {
                        initTokenChainOperation(client.pubkey, it)
                    }
                }
                .postAwaitConfirmation()
                .printResult(
                        "Network was initiated",
                        "Failed to initiate network",
                        printOnSuccess = true
                )
        if (economyChainConfigData != null) {
            initEconomyChain(client, config.chromiaClient)
        }
        if (tokenChainConfigData != null) {
            initTokenChain(client, config.chromiaClient)
        }
    }

    private fun readAndCompressConfigurationFromFile(client: PostchainClient, configFile: File, apiVersion: Long): ByteArray {
        val configData = BlockchainConfig.readFromFile(configFile)
        val compressedConfig = BlockchainConfigurationCompressor.compress(client, configData.gtv, apiVersion)
        return GtvEncoder.encodeGtv(compressedConfig)
    }
}
