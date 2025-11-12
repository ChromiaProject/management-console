package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.anchoring.anchoring_chain_common.getAnchoredBlockAtHeight
import net.postchain.base.configuration.BlockchainConfigurationData
import net.postchain.base.extension.CONFIG_HASH_EXTRA_HEADER
import net.postchain.base.gtv.BlockHeaderData
import net.postchain.chain0.nm_api.nmGetBlockchainConfigurationInfo
import net.postchain.chain0.proposal_blockchain.proposeRestoreOriginalConfigurationOperation
import net.postchain.common.BlockchainRid
import net.postchain.common.hexStringToByteArray
import net.postchain.common.wrap
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvDecoder
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchain.CommandProposeRestoreOriginalConfiguration.Companion.DISABLE_CHECKS_LONG_OPTION_NAME
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.heightOption
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeRestoreOriginalConfiguration : DCBaseCommand(
        name = "restore-original-configuration",
        help = """
        Proposes to restore a configuration for a blockchain
        To be used in case the current configuration in directory chain
        does not match what was actually used on chain.
        
        WARNING!!! This command will run basic verifications, but ensure you do not remove any chain-required
        configurations as this could break the chain.
        """.trimIndent(),
        requiresVersion = 103
) {
    companion object {
        const val DISABLE_CHECKS_LONG_OPTION_NAME = "--disable-checks"
    }

    private val blockchainConfigFile by option("-bc", "--blockchain-config", help = "Blockchain config to restore")
            .file(mustExist = true, mustBeReadable = true, canBeDir = false)
            .required()

    private val signers by option("-bs", "--blockchain-signers", help = "Comma separated list of signers to restore if necessary")
            .split(",")

    private val blockchainRID by blockchainRidOption().required()
    private val height by heightOption().required()
    private val description by proposalDescriptionOption { "Remove forced configuration in blockchain $blockchainRID on height $height" }
    private val disableChecks by option("-dc", DISABLE_CHECKS_LONG_OPTION_NAME, help = "Disable verification checks and ignore warnings").flag(default = false)

    override fun runDC() {

        val proposedConfig = BlockchainConfig.readFromFile(blockchainConfigFile)
        val proposedSigners = signers?.map { it.hexStringToByteArray() }

        if (!disableChecks) {
            validateRestoreOriginalConfiguration(blockchainRID, height, proposedConfig.gtv, proposedSigners)
        }
        transactionBuilder()
                .proposeRestoreOriginalConfigurationOperation(clientProviderPubkey, blockchainRID, height, proposedConfig.data, proposedSigners, description)
                .postOrSave()
                .printResult(
                        "Created proposal to restore configuration on blockchain RID $blockchainRID at height $height\nA node provider needs to approve this.",
                        "Cannot create proposal"
                )
    }
}

fun DCBaseCommand.validateRestoreOriginalConfiguration(blockchainRID: BlockchainRid, height: Long, proposedConfig: Gtv, proposedSigners: List<ByteArray>?) {
    val cacClient = config.chromiaClient.getClusterAnchoringClient(blockchainRID)
    val anchoredBlock = cacClient.getAnchoredBlockAtHeight(blockchainRID, height)
    if (anchoredBlock == null) {
        throw CliktError("Warning: Block at height $height is not yet anchored or built. Please ensure the configuration will not be in use when the proposal is approved. Run command again with $DISABLE_CHECKS_LONG_OPTION_NAME to ignore this warning.")
    } else {
        val chainConfigInfo = client.nmGetBlockchainConfigurationInfo(blockchainRID, height)!!
        val anchoredHeaderData = GtvDecoder.decodeGtv(anchoredBlock.blockHeader.data)
        val anchoredConfigHash = BlockHeaderData.fromGtv(anchoredHeaderData)
                .getExtra()[CONFIG_HASH_EXTRA_HEADER]!!.asByteArray()

        if (chainConfigInfo.configHash.data.contentEquals(anchoredConfigHash)) {
            throw CliktError("The configuration on height $height is valid and in use by chain $blockchainRID according to cluster anchoring chain ${cacClient.config.blockchainRid}")
        } else {
            echo("The configuration on height $height is confirmed not in use.")

            val configMap = proposedConfig.asDict().toMutableMap()
            configMap["signers"] = gtv((proposedSigners?.map { it.wrap() }
                    ?: chainConfigInfo.signers).map { gtv(it) })
            val proposedConfigHash = BlockchainConfigurationData.merkleHash(gtv(configMap))
            if (!proposedConfigHash.contentEquals(anchoredConfigHash)) {
                throw CliktError("The configuration on height $height in use by chain $blockchainRID does not match proposed configuration, according to cluster anchoring chain ${cacClient.config.blockchainRid}")
            }
        }
    }
}
