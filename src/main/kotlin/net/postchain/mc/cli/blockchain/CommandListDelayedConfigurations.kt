package net.postchain.mc.cli.blockchain

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getBlockchainInfo
import net.postchain.chain0.delay.listDelayedBlockchainConfigs
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.base.RID_LENGTH
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.proposal.showProposalInfo
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import java.time.Instant
import java.util.Date

class CommandListDelayedConfigurations : PmcCommand(
        name = "list-delayed-configurations",
        help = "List delayed configuration proposals for a blockchain"
) {
    private val config by pmcConfigOption()
    private val blockchainRID by blockchainRidOption().required()
    private val interactive by interactiveOption()

    private val headers = listOf("Proposal ID", "Type", "State", "Delay", "Apply at")

    override fun run() {
        val client = config.client
        val apiVersion = client.apiVersion()

        val blockchainInfo = client.getBlockchainInfo(blockchainRID.data)
        if (blockchainInfo == null) {
            throw CliktError("Blockchain not found")
        }

        if (blockchainInfo.configDelay == null || blockchainInfo.configDelay <= 0) {
            throw CliktError("No configuration delay is enabled for this blockchain")
        }

        when {
            apiVersion >= 63 -> {
                val proposals = client.listDelayedBlockchainConfigs(blockchainRID)
                echo(pmcTable(
                        "delayed configurations",
                        headers,
                        proposals.map {
                            listOf(
                                    it.proposalId.id.toString(),
                                    it.proposalType.name,
                                    it.proposalState.name,
                                    it.delay?.toString() ?: "N/A",
                                    if (it.applyAt != null) "${Date.from(Instant.ofEpochMilli(it.applyAt))}" else "N/A")
                        },
                        1 to RID_LENGTH,
                        interactive))
                if (interactive && proposals.isNotEmpty()) {
                    promptForIndex(proposals)?.let {
                        showProposalInfo(client, proposals[it].proposalId)
                    }
                }
            }

            else -> {
                throw CliktError("command requires directory chain version 63, found version $apiVersion")
            }
        }
    }
}