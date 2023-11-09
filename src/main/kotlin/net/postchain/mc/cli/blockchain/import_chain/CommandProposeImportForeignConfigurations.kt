package net.postchain.mc.cli.blockchain.import_chain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.common.queries.getBlockchains
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
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.hostOption
import net.postchain.mc.cli.portOption
import net.postchain.mc.cli.util.BlockchainConfigurationCompressor
import net.postchain.mc.cli.util.NopPostchainClient
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.pubkeyOption
import net.postchain.mc.network.requireApiVersion

class CommandProposeImportForeignConfigurations : CliktCommand(
        name = "import-foreign-configurations",
        help = """
            Propose importing a foreign blockchain configurations in a specific container 
            
            Change will be applied after voting within the deployer voter set 
            of the cluster that the container belongs to.
        """.trimIndent()
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val key by pubkeyOption("Node pubkey")

    private val host by hostOption().required()

    private val port by portOption().required()

    private val apiUrl by option("-a", "--api-url", help = "api url").required()

    private val chain0BlockchainRID by option("--chain0-blockchain-rid", help = "Chain0 blockchain RID")
            .convert { BlockchainRid.buildFromHex(it) }.required()

    private val blockchainRID by blockchainRidOption().required()

    private val name by nameOption("Name of blockchain").required()

    private val fromHeight by option("--from-height",
            help = "Only import configurations from and including this height (default is 0)"
    ).long().default(0L)

    private val upToHeight by option("--up-to-height",
            help = "Import configurations up to and including this height"
    ).long().required().validate {
        require(it > 0) { "--up-to-height arg must be greater than 0" }
    }

    private val container by option("-c", "--container", help = "Name of container to run in").required()

    private val description by proposalDescriptionOption(default = "Propose importing of foreign blockchain")

    override fun run() {
        val version = client.requireApiVersion(19)
        val foreignClient = buildForeignClient()
        val imported = mutableListOf<Long>()

        // propose foreign config import
        if (proposeImportBlockchain(foreignClient, version)) {
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
            proposeImportBlockchainConfigurations(next0, config, version)
            imported.add(next0)
            next = next0
        }

        echo("${imported.size} foreign configuration(s) imported")
    }

    private fun proposeImportBlockchain(foreignClient: PostchainClient, version: Long): Boolean {
        // ensure blockchain is IMPORTING if already added
        client.getBlockchains(true).firstOrNull {
            BlockchainRid(it.rid) == blockchainRID
        }?.also {
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
                .proposeForeignBlockchainImportOperation(client.pubkey,
                        key.data, host, port.toLong(), apiUrl,
                        chain0BlockchainRID.data,
                        name, blockchainRID, GtvEncoder.encodeGtv(compressedConfig), container, description
                )
                .postAwaitConfirmation()
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
                        client.pubkey,
                        blockchainRID,
                        height,
                        GtvEncoder.encodeGtv(compressedConfig),
                        "Propose importing of foreign blockchain configuration"
                )
                .postAwaitConfirmation()
                .printResult(
                        "Foreign configuration proposed at height $height",
                        "Failed to propose a foreign configuration at height $height", true
                )
    }

    private fun buildForeignClient(): PostchainClient = NopPostchainClient(PostchainClientImpl(
            client.config.copy(
                    blockchainRid = chain0BlockchainRID,
                    endpointPool = EndpointPool.singleUrl(apiUrl)
            )
    ))
}
