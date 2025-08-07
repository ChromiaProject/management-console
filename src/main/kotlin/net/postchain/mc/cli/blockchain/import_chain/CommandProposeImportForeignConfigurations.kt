package net.postchain.mc.cli.blockchain.import_chain

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.common.queries.getBlockchainInfo
import net.postchain.chain0.model.BlockchainState
import net.postchain.chain0.nm_api.nmFindNextConfigurationHeight
import net.postchain.chain0.nm_api.nmGetBlockchainConfiguration
import net.postchain.chain0.proposal_blockchain_import.proposeForeignBlockchainImportOperation
import net.postchain.chain0.proposal_blockchain_import.proposeImportConfigurationOperation
import net.postchain.client.core.PostchainClient
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.client.request.EndpointPool
import net.postchain.common.BlockchainRid
import net.postchain.gtv.GtvDecoder
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.portOption
import net.postchain.mc.cli.requiredHostOption
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.pubkeyOption
import net.postchain.mc.cli.util.requiredUrlOption

class CommandProposeImportForeignConfigurations : DCBaseCommand(
        name = "import-foreign-configurations",
        help = """
            Propose importing a foreign blockchain configurations in a specific container 
            
            Change will be applied after voting within the deployer voter set 
            of the cluster that the container belongs to.
        """.trimIndent(),
        requiresVersion = 19
) {
    private val key by pubkeyOption("Node pubkey")

    private val host by requiredHostOption()

    private val port by portOption().required()

    private val apiUrl by requiredUrlOption("api url", "-a", "--api-url")

    private val chain0BlockchainRID by option("--chain0-blockchain-rid", help = "Chain0 blockchain RID")
            .convert { BlockchainRid.buildFromHex(it) }.required()

    private val blockchainRID by blockchainRidOption().required()

    private val name by nameOption("Name of blockchain").required().validate(entityNameValidator())

    private val fromHeight by option("--from-height",
            help = "Only import configurations from and including this height (default is 0)"
    ).long().default(0L)

    private val upToHeight by option("--up-to-height",
            help = "Import configurations up to and including this height"
    ).long().required().validate {
        require(it > 0) { "--up-to-height arg must be greater than 0" }
    }

    private val container by option("-c", "--container", help = "Name of container to run in").required()

    private val description by proposalDescriptionOption {
        "Import foreign blockchain configurations - node pubkey: $key, host: $host, port: $port, api-url: $apiUrl, " +
                "chain0-blockchain-rid: $chain0BlockchainRID, name: $name, blockchain-rid: $blockchainRID, " +
                "from-height: $fromHeight, up-to-height: $upToHeight"
    }

    override fun runDC() {
        val foreignClient = buildForeignClient()
        val imported = mutableListOf<Long>()

        // propose foreign config import
        if (proposeImportBlockchain(foreignClient, dcVersion)) {
            imported.add(0)
        }

        // propose configurations
        var next = fromHeight
        while (true) {
            val next0 = foreignClient.nmFindNextConfigurationHeight(blockchainRID, next)
            if (next0 == null || upToHeight in 1 until next0) break
            echo("Foreign configuration found at height $next0")

            val config = foreignClient.nmGetBlockchainConfiguration(blockchainRID, next0)
                    ?: throw CliktError("Can't get blockchain configuration at height $next0")
            echo("Foreign configuration at height $next0 downloaded")
            proposeImportBlockchainConfigurations(next0, config, dcVersion)
            imported.add(next0)
            next = next0
        }

        echo("${imported.size} foreign configuration(s) imported")
    }

    private fun proposeImportBlockchain(foreignClient: PostchainClient, version: Long): Boolean {
        // ensure blockchain is IMPORTING if already added
        client.getBlockchainInfo(blockchainRID.data)?.also {
            if (it.state != BlockchainState.IMPORTING) {
                throw CliktError("Configurations import is allowed only for blockchain in ${BlockchainState.IMPORTING} state")
            } else {
                echo("Foreign blockchain import already proposed")
                return false
            }
        }

        val configData0 = foreignClient.nmGetBlockchainConfiguration(blockchainRID, 0)
                ?: throw CliktError("Can't download the foreign blockchain initial configuration")
        echo("Initial configuration of foreign blockchain downloaded")
        val compressedConfig = BlockchainConfigurationCompressor.compress(client, GtvDecoder.decodeGtv(configData0), version)

        client.transactionBuilder()
                .proposeForeignBlockchainImportOperation(clientProviderPubkey,
                        key.data, host, port.toLong(), apiUrl,
                        chain0BlockchainRID.data,
                        name, blockchainRID, GtvEncoder.encodeGtv(compressedConfig), container, description
                )
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Foreign blockchain import proposed",
                        "Failed to propose a foreign blockchain", true
                )
        return true
    }

    private fun proposeImportBlockchainConfigurations(height: Long, configData: ByteArray, version: Long) {
        val compressedConfig = BlockchainConfigurationCompressor.compress(client, GtvDecoder.decodeGtv(configData), version)
        client.transactionBuilder()
                .proposeImportConfigurationOperation(
                        clientProviderPubkey,
                        blockchainRID,
                        height,
                        GtvEncoder.encodeGtv(compressedConfig),
                        "Import foreign blockchain configuration - blockchain-rid: $blockchainRID, height: $height"
                )
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Foreign configuration proposed at height $height",
                        "Failed to propose a foreign configuration at height $height", true
                )
    }

    private fun buildForeignClient(): PostchainClient = PostchainClientImpl(
            client.config.copy(
                    blockchainRid = chain0BlockchainRID,
                    endpointPool = EndpointPool.singleUrl(apiUrl)
            )
    )
}
