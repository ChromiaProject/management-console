package net.postchain.mc.cli.blockchain

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.proposal_blockchain.proposeForcedConfigurationOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.heightOption
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.compatibility.ApiCompatV78.proposeForcedConfigurationOperationV78
import net.postchain.mc.network.requireApiVersion


class CommandProposeForcedConfiguration : DCBaseCommand(
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
        """.trimIndent(),
        requiresVersion = 40
) {
    private val blockchainConfigFile by option("-bc", "--blockchain-config", help = "Blockchain config to force")
            .file(mustExist = true, mustBeReadable = true, canBeDir = false)
            .required()

    private val blockchainRID by blockchainRidOption().required()

    private val height by heightOption().required()

    private val resumeChain by option("-r", "--resume", help = "Automatically resume blockchain after configuration is applied").flag()

    private val description by proposalDescriptionOption { "Force update of blockchain configuration for $blockchainRID at height $height" }

    override fun runDC() {
        if (resumeChain) {
            client.requireApiVersion(80, message = "--resume")
        }

        val bcConfig = BlockchainConfig.readFromFile(blockchainConfigFile)
        val compressedConfigurationData = GtvEncoder.encodeGtv(BlockchainConfigurationCompressor.compress(client, bcConfig.gtv, dcVersion))

        client.transactionBuilder()
                .apply {
                    if (dcVersion >= 80) {
                        proposeForcedConfigurationOperation(clientProviderPubkey, blockchainRID, compressedConfigurationData, height, description, resumeChain)
                    } else {
                        proposeForcedConfigurationOperationV78(clientProviderPubkey, blockchainRID, compressedConfigurationData, height, description)
                    }
                }
                .postAwaitConfirmation()
                .printResult(
                        "Forced configurations was proposed: ${bcConfig.hash}",
                        "Failed to propose forced configuration"
                )
    }
}
