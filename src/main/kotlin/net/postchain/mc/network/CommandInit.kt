package net.postchain.mc.network

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.chromia.build.tools.config.ChromiaConfigLoader
import com.chromia.build.tools.config.ChromiaConfigWriter
import com.chromia.cli.tools.config.chromiaConfigFileOption
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.common.init.initOperation
import net.postchain.chain0.economy_chain_in_directory_chain.initEconomyChainOperation
import net.postchain.chain0.token_chain_in_directory_chain.initTokenChainOperation
import net.postchain.client.config.PostchainClientConfig
import net.postchain.client.core.PostchainClient
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.common.BlockchainRid
import net.postchain.common.config.getEnvOrStringProperty
import net.postchain.d1.client.StandardChromiaClient
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.defaultClientConfig
import java.io.File

class CommandInit : PmcCommand(
        name = "initialize",
        help = "Create system cluster with naked system container for the directory blockchain. Module argument initial_provider becomes first member of SYSTEM_P voter set."
) {
    val lookupBrid by option("--lookup-brid", help = "Ignore any 'brid' property in configuration file, always perform lookup").flag()
    val configFile by chromiaConfigFileOption()
    val rawConfig by lazy { ChromiaConfigLoader(::echo).loadProperties(configFile) }
    val client by lazy {
        val configuredBrid = if (lookupBrid) null else rawConfig.getEnvOrStringProperty("POSTCHAIN_CLIENT_BLOCKCHAIN_RID", "brid")
        rawConfig.setProperty("brid", configuredBrid ?: BlockchainRid.ZERO_RID.toHex())

        if (!rawConfig.containsKey("api.url")) throw CliktError("No api.url specified")
        val initialConfig = PostchainClientConfig.fromConfiguration(rawConfig, defaultClientConfig)

        val dcConfig = if (initialConfig.blockchainRid != BlockchainRid.ZERO_RID)
            initialConfig
        else
            initialConfig.copy(blockchainRid = PostchainClientImpl(initialConfig).getBlockchainRID(0))

        if (configuredBrid == null) {
            ChromiaConfigWriter.local.setBrid(dcConfig.blockchainRid)
        }

        PostchainClientImpl(dcConfig)
    }

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

        if (economyChainConfigData != null || tokenChainConfigData != null) {
            val chromiaClient = StandardChromiaClient(client.config)
            val dcClient = chromiaClient.getDirectoryChainClient()
            if (economyChainConfigData != null) {
                initEconomyChain(dcClient, chromiaClient)
            }
            if (tokenChainConfigData != null) {
                initTokenChain(dcClient, chromiaClient)
            }
        }
    }

    private fun readAndCompressConfigurationFromFile(client: PostchainClient, configFile: File, apiVersion: Long): ByteArray {
        val configData = BlockchainConfig.readFromFile(configFile)
        val compressedConfig = BlockchainConfigurationCompressor.compress(client, configData.gtv, apiVersion)
        return GtvEncoder.encodeGtv(compressedConfig)
    }
}
