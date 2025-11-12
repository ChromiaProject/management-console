package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.getRestoreOriginalConfigurationStage2Proposal
import net.postchain.gtv.GtvDecoder.decodeGtv
import net.postchain.gtv.GtvDictionary
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.gtv.diff.GtvDiffFinder

class CommandGetProposedRestoreOriginalConfiguration : DCBaseCommand(
        name = "get-proposed-restore-original-configuration",
        help = "Get proposed restoration of original configuration",
        requiresVersion = 104
) {
    private val blockchainRID by blockchainRidOption().required()

    override fun runDC() {
        val p = client.getRestoreOriginalConfigurationStage2Proposal(blockchainRID)
        if (p != null) {
            val currentConfig = p.currentConfiguration?.let { (decodeGtv(it.data) as GtvDictionary) }
                    ?: GtvDictionary.build(emptyMap())
            val originalConfig = decodeGtv(p.originalConfiguration.data) as GtvDictionary
            echo("Proposed restoration of original configuration for blockchain ${p.blockchainRid.toHex()} at height ${p.height}:\n\n${GtvDiffFinder.diff(currentConfig, originalConfig).diff}${if (p.originalSigners != null) "\n\nRestored signers: ${p.originalSigners}" else ""}")
        } else {
            echo("No configuration restoration proposals for blockchain ${blockchainRID.toHex()}")
        }
    }
}
