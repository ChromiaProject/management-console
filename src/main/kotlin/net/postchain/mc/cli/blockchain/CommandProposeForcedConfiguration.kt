package net.postchain.mc.cli.blockchain

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.terminal.prompt
import net.postchain.chain0.common.queries.getBlockchainInfo
import net.postchain.chain0.model.BlockchainState
import net.postchain.chain0.proposal_blockchain.proposeForcedConfigurationOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.heightOption
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.nullableProposalDescriptionOption
import net.postchain.mc.compatibility.ApiCompatV78.proposeForcedConfigurationOperationV78
import net.postchain.mc.network.requireApiVersion


class CommandProposeForcedConfiguration : DCBaseCommand(
        name = "force-update",
        help = """
        Propose a new forced configuration to a blockchain
        
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

    private val height by heightOption()

    private val detectHeight by option("-dh", "--detect-height", help = "Detect and suggest the height").flag()

    private val resumeChain by option("-r", "--resume", help = "Automatically resume blockchain after configuration is applied").flag()

    private val description by nullableProposalDescriptionOption()

    override fun runDC() {
        if (resumeChain) {
            client.requireApiVersion(dcVersion, 80, message = "--resume")
        }

        if ((height != null) == detectHeight) {
            throw UsageError("You must specify --height or --detect-height")
        }

        val proposalHeight: Long = height ?: let {
            val blockchainInfo = client.getBlockchainInfo(blockchainRID.data) ?: throw CliktError("Blockchain not found")
            if (blockchainInfo.state != BlockchainState.PAUSED) {
                throw UsageError("Blockchain is in state ${blockchainInfo.state} but must be ${BlockchainState.PAUSED} to detect the height")
            }
            val chainClient = config.chromiaClient.getClient(blockchainRID)
            val currentHeight = chainClient.currentBlockHeight()
            if (terminal.terminalInfo.inputInteractive) {
                val answer = terminal.prompt("The blockchain is ${BlockchainState.PAUSED} and about to build block $currentHeight.\n\nDo you want to proceed and create a forced configuration proposal for height $currentHeight? (y/N)")
                if (answer == null || !answer.startsWith("Y", ignoreCase = true))
                    throw CliktError("Canceled", statusCode = 0)
            }
            currentHeight
        }
        val proposalDescription = description ?: "Force update of blockchain configuration for $blockchainRID at height $proposalHeight"

        val bcConfig = BlockchainConfig.readFromFile(blockchainConfigFile)
        val compressedConfigurationData = GtvEncoder.encodeGtv(BlockchainConfigurationCompressor.compress(client, bcConfig.gtv, dcVersion))

        transactionBuilder()
                .apply {
                    if (dcVersion >= 80) {
                        proposeForcedConfigurationOperation(clientProviderPubkey, blockchainRID, compressedConfigurationData, proposalHeight, proposalDescription, resumeChain)
                    } else {
                        proposeForcedConfigurationOperationV78(clientProviderPubkey, blockchainRID, compressedConfigurationData, proposalHeight, proposalDescription)
                    }
                }
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Forced configurations was proposed: ${bcConfig.hash}" + (if (dcVersion >=83) "\nA node provider needs to approve this." else ""),
                        "Failed to propose forced configuration"
                )
    }
}
