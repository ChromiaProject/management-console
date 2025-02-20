package net.postchain.mc.cli.blockchain.import_chain

import com.github.ajalt.clikt.core.CliktError
import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.deprecated
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.nm_api.nmFindNextConfigurationHeight
import net.postchain.chain0.proposal_blockchain_import.proposeFinishImportBlockchainOperation
import net.postchain.common.BlockchainRid
import net.postchain.gtv.GtvDecoder
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.configurationsFileOption
import net.postchain.mc.cli.util.nullableProposalDescriptionOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.network.requireApiVersion
import java.io.BufferedInputStream
import java.io.FileInputStream

class CommandProposeFinishBlockchainImport : PmcCommand(
        name = "finish-import",
        help = """
            Propose finishing import of a blockchain
            
            Change will be applied after voting within the deployer voter set 
            of the cluster that the container belongs to.
        """.trimIndent()
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val configurationsFile by configurationsFileOption().required()

    private val finishAtHeight by option("--finish-at-height", help = "Finish blockchain import at height (required for API version 18 and later)")
            .deprecated("Use --final-height option")

    private val finalHeight by option("--final-height", help = "Finish blockchain import at height (required for API version 33 and later)")
            .long().required().validate {
                require(it > 0) { "--final-height arg must be greater than 0" }
            }

    private val description by nullableProposalDescriptionOption()

    private fun description(blockchainRID: BlockchainRid) = description ?: run {
        "Finish blockchain import from file - blockchain-rid: $blockchainRID, final-height: $finalHeight"
    }

    override fun run() {
        client.requireApiVersion(19)
        val (missingConfigHeights, blockchainRID) = getMissingConfigHeights()
        if (missingConfigHeights.isNotEmpty()) {
            throw CliktError("Cannot finish blockchain import. Configurations for height(s): ${missingConfigHeights.joinToString(", ")} have not been imported yet.")
        } else {
            echo("Import of blockchain ${blockchainRID.toHex()} will be finished")
            val txBuilder = client.transactionBuilder()
            txBuilder.proposeFinishImportBlockchainOperation(client.pubkey, blockchainRID, finalHeight, description(blockchainRID))
            txBuilder.postAwaitConfirmation().printResult(
                    "Import of blockchain ${blockchainRID.toHex()} finished",
                    "Cannot finish blockchain import", true
            )
        }
    }

    private fun getMissingConfigHeights(): Pair<Set<Long>, BlockchainRid> =
            BufferedInputStream(FileInputStream(configurationsFile.toFile())).use { inputStream ->
                val blockchainRID = BlockchainRid(GtvDecoder.decodeGtv(inputStream).asByteArray())

                val configHeights = mutableSetOf<Long>()
                while (true) {
                    val gtv = GtvDecoder.decodeGtv(inputStream)
                    if (gtv.isNull()) {
                        break
                    }
                    configHeights.add(gtv.asArray()[0].asInteger())
                }

                val heights = mutableSetOf(0L)
                while (true) {
                    val nextHeight = client.nmFindNextConfigurationHeight(blockchainRID, heights.last()) ?: break
                    heights.add(nextHeight)
                }

                val missingConfigHeights = (configHeights - heights).filter { it <= finalHeight }.toSet()

                return missingConfigHeights to blockchainRID
            }

}
