package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.anchoring.anchoring_chain_common.getAnchoredBlockAtHeight
import net.postchain.base.extension.CONFIG_HASH_EXTRA_HEADER
import net.postchain.base.gtv.BlockHeaderData
import net.postchain.chain0.nm_api.nmGetBlockchainConfigurationInfo
import net.postchain.chain0.proposal_blockchain.proposeRemoveForcedConfigurationOperation
import net.postchain.gtv.GtvDecoder
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.heightOption
import net.postchain.mc.cli.util.proposalDescriptionOption


class CommandProposeRemoveForcedConfiguration : DCBaseCommand(
        name = "remove-forced-configuration",
        help = """
        Proposes to remove a forced configuration from the blockchain
        
        WARNING!!! This command will run basic verifications, but ensure you do not remove any chain-required
        configurations as this could break the chain.
        """.trimIndent(),
        requiresVersion = 82
) {
    companion object {
        const val DISABLE_CHECKS_LONG_OPTION_NAME = "--disable-checks"
    }

    private val blockchainRID by blockchainRidOption().required()
    private val height by heightOption().required()
    private val description by proposalDescriptionOption { "Remove forced configuration in blockchain $blockchainRID on height $height" }
    private val disableChecks by option("-dc", DISABLE_CHECKS_LONG_OPTION_NAME, help = "Disable verification checks and ignore warnings").flag(default = false)

    override fun runDC() {

        val cacClient = config.chromiaClient.getClusterAnchoringClient(blockchainRID)
        val anchoredBlock = cacClient.getAnchoredBlockAtHeight(blockchainRID, height)
        if (anchoredBlock == null) {
            if (!disableChecks) {
                throw CliktError("Warning: Block at height $height is not yet anchored or built. Please ensure the configuration will not be in use when the proposal is approved. Run command again with $DISABLE_CHECKS_LONG_OPTION_NAME to ignore this warning.")
            }
        } else {

            val chainConfigHash = client.nmGetBlockchainConfigurationInfo(blockchainRID, height)!!.configHash
            val anchoredHeaderData = GtvDecoder.decodeGtv(anchoredBlock.blockHeader.data)
            val anchoredConfigHash = BlockHeaderData.fromGtv(anchoredHeaderData).getExtra()[CONFIG_HASH_EXTRA_HEADER]

            if (chainConfigHash.data.contentEquals(anchoredConfigHash!!.asByteArray())) {
                throw CliktError("The configuration on height $height is valid and in use by chain $blockchainRID according to cluster anchoring chain ${cacClient.config.blockchainRid}")
            } else {
                echo("The configuration on height $height is confirmed not in use.")
            }
        }

        client.transactionBuilder()
                .proposeRemoveForcedConfigurationOperation(blockchainRID, height, description)
                .postAwaitConfirmation()
                .printResult(
                        "Created proposal to remove forced configuration on blockchain RID $blockchainRID at height $height",
                        "Cannot create proposal"
                )
    }
}
