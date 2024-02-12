package net.postchain.mc.cli.economy.proposal

import com.chromia.directory1.common.queries.getProviderData
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.mordant.table.SectionBuilder
import net.postchain.client.core.PostchainClient
import net.postchain.common.types.RowId
import net.postchain.common.types.WrappedByteArray
import net.postchain.crypto.PubKey
import net.postchain.economy.economy_chain.getClusterChangeTagProposal
import net.postchain.economy.economy_chain.getClusterCreateProposal
import net.postchain.economy.economy_chain.getTagProposal
import net.postchain.economy.economy_chain.ec_proposal.EcProposalType
import net.postchain.economy.economy_chain.ec_proposal.EcProposalVotingResults
import net.postchain.economy.economy_chain.ec_proposal.GetProposalResult
import net.postchain.economy.economy_chain.ec_proposal.ProposalState
import net.postchain.economy.economy_chain.ec_proposal.getProposal
import net.postchain.economy.economy_chain.ec_proposal.getProposalVoterInfo
import net.postchain.economy.economy_chain.ec_proposal.getProposalVotingResults
import net.postchain.economy.economy_chain.getEcononyConstantsProposal
import net.postchain.mc.cli.economy.ECBaseCommand
import net.postchain.mc.cli.proposal.util.proposalIndexOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.cli.votingupdates.formatThreshold
import java.time.Instant
import java.util.Date

class CommandGetProposal : ECBaseCommand(
        name = "info",
        help = "Gets information of a given proposal"
) {
    private val id by proposalIndexOption().convert { RowId(it) }

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        showECProposalInfo(client, economyChainClient, id)
    }
}

fun CliktCommand.showECProposalInfo(client: PostchainClient, economyChainClient: PostchainClient, id: RowId?) {

    val proposal = economyChainClient.getProposal(id) ?: return echo("Proposal $id not found")
    val proposedBy = client.getProviderData(PubKey(proposal.proposedBy))

    echo(pmcTable {
        body {
            printECProposalHeader(proposal.id, proposal.type, proposal.timestamp, proposedBy.pubkey, proposedBy.name)
            row("State", proposal.state.toString())
            printECVotingInfo(economyChainClient, proposal)
            row("Description", proposal.description)
        }
    })

    if (proposal.state == ProposalState.PENDING) {
        if (terminal.info.outputInteractive) echo("Proposal details")
        echo(formatECPendingProposal(economyChainClient, proposal.id, proposal.type))
    }
}

private fun SectionBuilder.printECVotingInfo(economyChainClient: PostchainClient, proposal: GetProposalResult) {
    if (proposal.state == ProposalState.PENDING) {
        printECVotingResults(economyChainClient.getProposalVotingResults(proposal.id))
    } else {
        val votingInfo = economyChainClient.getProposalVoterInfo(proposal.id)
        row("Providers that accepted", votingInfo.filter { it.vote }.joinToString { formatProvider(it.provider, "") })
        row("Providers that rejected", votingInfo.filterNot { it.vote }.joinToString { formatProvider(it.provider, "") })
    }
}

private fun SectionBuilder.printECVotingResults(votingResults: EcProposalVotingResults) {
    row("Positive votes", votingResults.positiveVotes.toString())
    row("Negative votes", votingResults.negativeVotes.toString())
    row("Max votes", votingResults.maxVotes.toString())
    row("Threshold", formatThreshold(votingResults.threshold))
    row("Status", votingResults.votingResult.toString())
}

private fun SectionBuilder.printECProposalHeader(id: RowId, type: EcProposalType, timestamp: Long, proposedByPubkey: WrappedByteArray, proposedByName: String) {
    row("Proposal", "${id.id} - ${type.name}")
    row("Proposed by", formatProvider(proposedByPubkey, proposedByName))
    row("Time", "${Date.from(Instant.ofEpochMilli(timestamp))}")
}

private fun formatProvider(providerPubKey: WrappedByteArray, providerName: String) =
        "${providerPubKey.toHex()}${if (providerName.isNotEmpty()) " - $providerName" else ""}"

private fun CliktCommand.formatECPendingProposal(economyChainClient: PostchainClient, proposalId: RowId, proposalType: EcProposalType): Any {
    when (proposalType) {
        EcProposalType.tag_create, EcProposalType.tag_update, EcProposalType.tag_remove -> {

            val tagCreateProposal = economyChainClient.getTagProposal(proposalId)
            return pmcTable {
                body {
                    row("Name", tagCreateProposal.name)
                    if (tagCreateProposal.scuPrice != -1L) { row("SCU price", tagCreateProposal.scuPrice) }
                    if (tagCreateProposal.extraStoragePrice != -1L) row("Extra storage price", tagCreateProposal.extraStoragePrice)
                }
            }
        }
        EcProposalType.cluster_create -> {

            val clusterCreateProposal = economyChainClient.getClusterCreateProposal(proposalId)
            return pmcTable {
                body {
                    row("Name", clusterCreateProposal.name)
                    row("Tag", clusterCreateProposal.tag)
                    row("Proposer", clusterCreateProposal.proposerPubkey)
                    row("Governor voter set", clusterCreateProposal.governorVoterSetName)
                    row("Voter set", clusterCreateProposal.voterSetName)
                    row("Cluster units", clusterCreateProposal.clusterUnits)
                    row("Extra storage", clusterCreateProposal.extraStorage)
                    row("Status", clusterCreateProposal.status.name)
                }
            }
        }

        EcProposalType.cluster_change_tag -> {

            val clusterChangeTagProposal = economyChainClient.getClusterChangeTagProposal(proposalId)
            return pmcTable {
                body {
                    row("Cluster name", clusterChangeTagProposal.cluster)
                    row("Current tag", clusterChangeTagProposal.currentTag)
                    row("New tag", clusterChangeTagProposal.newTag)
                }
            }
        }

        EcProposalType.economy_constants_update -> {
            val economyConstantsProposal = economyChainClient.getEcononyConstantsProposal(proposalId)
            return pmcTable {
                body {
                    if (economyConstantsProposal.minLeaseTimeWeeks != null) { row("Min lease time weeks", economyConstantsProposal.minLeaseTimeWeeks) }
                    if (economyConstantsProposal.maxLeaseTimeWeeks != null) { row("Max lease time weeks", economyConstantsProposal.maxLeaseTimeWeeks) }
                    if (economyConstantsProposal.stakingRewardFeeShare != null) { row("Staking reward fee share", economyConstantsProposal.stakingRewardFeeShare) }
                    if (economyConstantsProposal.chromiaFoundationFeeShare != null) { row("Chromia foundation fee share", economyConstantsProposal.chromiaFoundationFeeShare) }
                    if (economyConstantsProposal.resourcePoolMarginFeeShare != null) { row("Resource pool margin feee share", economyConstantsProposal.resourcePoolMarginFeeShare) }
                    if (economyConstantsProposal.dappProviderRiskShare != null) { row("Dapp provider risk share", economyConstantsProposal.dappProviderRiskShare) }
                }
            }
        }

        else -> return "No details for proposal"
    }
}
