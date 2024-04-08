package net.postchain.mc.cli.blockchain

import com.chromia.cli.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.proposal_blockchain.proposeForcedConfigurationOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.heightOption
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.network.Version


class CommandProposeForcedConfiguration : CliktCommand(
        name = "force-update",
        help = """
        Propose a new forced configuration to a blockchain.
        
        WARNING!!! Only use this if absolutely necessary.
        Command is irreversible but forced configs can be overwritten.
        
        Forced configurations can only be proposed for PAUSED blockchains.
        Specified height must be the current blockchain height in order that the blockchain to be able to be RESUMED successfully.
        WARNING!!! Using a different height could cause problems with replicating the blockchain.
        Signers lists will be updated with the current cluster's nodes.
        Pending configurations for this blockchain will be removed.
        """.trimIndent()
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainConfigFile by option("-bc", "--blockchain-config", help = "Blockchain config to force")
            .file(mustExist = true, mustBeReadable = true, canBeDir = false)
            .required()

    private val blockchainRID by blockchainRidOption().required()

    private val height by heightOption().required()

    private val description by proposalDescriptionOption()

    override fun run() {
        val version = Version(client)
        if (version.version < 40) {
            echo("Force update requires directory chain version 40, found version $version")
            return
        }

        val bcConfig = BlockchainConfig.readFromFile(blockchainConfigFile)
        val compressedConfigurationData = GtvEncoder.encodeGtv(BlockchainConfigurationCompressor.compress(client, bcConfig.gtv, version.version))

        client.transactionBuilder()
                .apply {
                   proposeForcedConfigurationOperation(client.config.pubkey().data, blockchainRID, compressedConfigurationData, height, description)
                }
                .postAwaitConfirmation()
                .printResult(
                        "Forced configurations was proposed: ${bcConfig.hash}",
                        "Failed to propose forced configuration"
                )
    }
}
