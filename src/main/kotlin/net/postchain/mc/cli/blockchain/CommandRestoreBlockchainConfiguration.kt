package net.postchain.mc.cli.blockchain

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.anchoring.anchoring_chain_common.getAnchoredBlockAtHeight
import net.postchain.anchoring.anchoring_chain_common.getAnchoringTransactionForBlockRid
import net.postchain.chain0.GtxOperation
import net.postchain.chain0.GtxTransaction
import net.postchain.chain0.GtxTransactionBody
import net.postchain.chain0.common.operations.restoreBlockchainConfigurationOperation
import net.postchain.client.core.TxRid
import net.postchain.client.impl.ConfirmationProofData
import net.postchain.common.hexStringToByteArray
import net.postchain.common.wrap
import net.postchain.gtv.GtvArray
import net.postchain.gtv.GtvDecoder
import net.postchain.gtv.GtvEncoder
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.toObject
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.heightOption
import net.postchain.mc.cli.util.BlockchainConfig

class CommandRestoreBlockchainConfiguration : DCBaseCommand(
        name = "restore-configuration",
        help = """
        Propose restoring blockchain configuration to what was used in anchored block, either by removal or replacement
        
        In the case that a forced configuration update has been incorrectly applied a node provider in the same cluster
        can heal the state in directory chain by posting a proof of which config that was anchored.
        """.trimIndent(),
        requiresVersion = 104
) {

    private val blockchainRid by blockchainRidOption().required()
    private val height by heightOption().required()

    private val blockchainConfigFile by option("-bc", "--blockchain-config", help = "Optional blockchain config to replace existing config with")
            .file(mustExist = true, mustBeReadable = true, canBeDir = false)

    private val signerList by option("-bcs", "--blockchain-signers", help = "Optional comma separated list of public keys of signers if they are necessary to replace")
            .split(",")

    private val clusterName by option(
            "-c", "--cluster",
            help = "In case the blockchain has been removed you need to manually supply which cluster it was running in at the given height"
    )

    override fun runDC() {
        val signers = signerList?.map { it.hexStringToByteArray() }
        val blockchainConfig = if (blockchainConfigFile != null)
            GtvEncoder.encodeGtv(BlockchainConfigurationCompressor.compress(client, BlockchainConfig.readFromFile(blockchainConfigFile!!).gtv, dcVersion))
        else null

        val cacClient = clusterName?.let { config.chromiaClient.getClusterAnchoringClient(it) }
                ?: config.chromiaClient.getClusterAnchoringClient(blockchainRid)
        val anchoredBlock = cacClient.getAnchoredBlockAtHeight(blockchainRid, height)
                ?: throw CliktError("No anchored block at height $height for blockchain $blockchainRid")
        val anchoringTxWithOpIndex = cacClient.getAnchoringTransactionForBlockRid(blockchainRid, anchoredBlock.blockRid.data)!!
        val anchoringTxProof = cacClient.confirmationProof(TxRid(anchoringTxWithOpIndex.txRid.toHex()))
        val decodedAnchoringTxProof = GtvDecoder.decodeGtv(anchoringTxProof).toObject<ConfirmationProofData>()

        transactionBuilder()
                .addOperation(
                        "iccf_proof",
                        gtv(cacClient.config.blockchainRid),
                        gtv(decodedAnchoringTxProof.hash),
                        gtv(anchoringTxProof)
                )
                .restoreBlockchainConfigurationOperation(
                        clientProviderPubkey,
                        blockchainRid, height,
                        gtxTransactionFromGtvArray(GtvDecoder.decodeGtv(anchoringTxWithOpIndex.txData.data) as GtvArray),
                        anchoringTxWithOpIndex.txOpIndex,
                        blockchainConfig, signers
                )
                .postOrSave()
                .printResult(
                        "Blockchain configuration was successfully restored",
                        "Blockchain configuration restoration failed"
                )
    }

    private fun gtxTransactionFromGtvArray(gtv: GtvArray) = GtxTransaction(
            gtxTransactionBodyFromGtvArray(gtv[0] as GtvArray),
            gtv[1].asArray().toList()
    )

    private fun gtxTransactionBodyFromGtvArray(gtv: GtvArray) = GtxTransactionBody(
            gtv[0].asByteArray().wrap(),
            gtv[1].asArray().map { gtxOperationFromGtvArray(it as GtvArray) },
            gtv[2].asArray().toList()
    )

    private fun gtxOperationFromGtvArray(gtv: GtvArray) = GtxOperation(
            gtv[0].asString(),
            gtv[1].asArray().toList()
    )
}
