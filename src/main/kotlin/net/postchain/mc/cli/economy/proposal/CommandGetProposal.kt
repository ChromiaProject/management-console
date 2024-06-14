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
import net.postchain.economy.common_proposal.CommonProposalState
import net.postchain.economy.common_proposal.CommonProposalType
import net.postchain.economy.common_proposal.CommonProposalVoter
import net.postchain.economy.common_proposal.CommonProposalVotingResults
import net.postchain.economy.common_proposal.CommonVotingResult
import net.postchain.economy.common_proposal.GetCommonProposalResult
import net.postchain.economy.common_proposal.getCommonProposal
import net.postchain.economy.common_proposal.getCommonProposalVoterInfo
import net.postchain.economy.common_proposal.getCommonProposalVotingResults
import net.postchain.economy.economy_chain.apiVersion
import net.postchain.economy.economy_chain.getClusterChangeTagProposal
import net.postchain.economy.economy_chain.getClusterCreateProposal
import net.postchain.economy.economy_chain.getEcVoterSetUpdateProposal
import net.postchain.economy.economy_chain.getEcononyConstantsProposal
import net.postchain.economy.economy_chain.getMintingProposal
import net.postchain.economy.economy_chain.getStakingRequirementConstantsProposal
import net.postchain.economy.economy_chain.getSystemProviderEconomyConstantsProposal
import net.postchain.economy.economy_chain.getTagProposal
import net.postchain.mc.cli.base.rowIfNotNull
import net.postchain.mc.cli.economy.ECBaseCommand
import net.postchain.mc.cli.economy.ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION
import net.postchain.mc.cli.proposal.util.proposalIndexOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.cli.votingupdates.formatThreshold
import net.postchain.mc.compatibility.ApiCompatECV21.EcProposalTypeECV20
import net.postchain.mc.compatibility.ApiCompatECV21.getProposalECV20
import net.postchain.mc.compatibility.ApiCompatECV21.getProposalVoterInfoECV20
import net.postchain.mc.compatibility.ApiCompatECV21.getProposalVotingResultsECV20
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

fun CliktCommand.showECProposalInfo(client: PostchainClient, economyChainClient: PostchainClient, id: RowId?, version: Long = economyChainClient.apiVersion()) {

    val proposal = when {
        version < ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION -> economyChainClient.getProposalECV20(id).let {
            if (it != null)
                GetCommonProposalResult(it.id, it.timestamp, mapType(it.type), it.proposedBy, it.description, CommonProposalState.valueOf(it.state.name))
            else
                null
        }
        else -> economyChainClient.getCommonProposal(id)
    } ?: return echo("Proposal $id not found")
    val proposedBy = client.getProviderData(PubKey(proposal.proposedBy))

    echo(pmcTable {
        body {
            printECProposalHeader(proposal.id, proposal.type, proposal.timestamp, proposedBy.pubkey, proposedBy.name)
            row("State", proposal.state.toString())
            printECVotingInfo(economyChainClient, proposal, version)
            row("Description", proposal.description)
        }
    })

    if (proposal.state == CommonProposalState.PENDING) {
        if (terminal.info.outputInteractive) echo("Proposal details")
        echo(formatECPendingProposal(economyChainClient, proposal.id, proposal.type))
    }
}

fun mapType(type: EcProposalTypeECV20): CommonProposalType {

    return when (type) {
        EcProposalTypeECV20.cluster_create -> CommonProposalType.ec_cluster_create
        EcProposalTypeECV20.cluster_change_tag -> CommonProposalType.ec_cluster_change_tag
        EcProposalTypeECV20.tag_create -> CommonProposalType.ec_tag_create
        EcProposalTypeECV20.tag_update -> CommonProposalType.ec_tag_update
        EcProposalTypeECV20.tag_remove -> CommonProposalType.ec_tag_remove
        EcProposalTypeECV20.economy_constants_update -> CommonProposalType.ec_constants_update
    }
}

private fun SectionBuilder.printECVotingInfo(economyChainClient: PostchainClient, proposal: GetCommonProposalResult, version: Long) {
    if (proposal.state == CommonProposalState.PENDING) {
        val votingResults = when {
            version < ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION -> economyChainClient.getProposalVotingResultsECV20(proposal.id).let {
                CommonProposalVotingResults(it.positiveVotes, it.negativeVotes, it.maxVotes, it.threshold, CommonVotingResult.valueOf(it.votingResult.name))
            }
            else -> economyChainClient.getCommonProposalVotingResults(proposal.id)
        }
        printECVotingResults(votingResults)
    } else {
        val votingInfo = when {
            version < ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION -> economyChainClient.getProposalVoterInfoECV20(proposal.id)
                    .map { CommonProposalVoter(it.provider, it.vote) }
            else -> economyChainClient.getCommonProposalVoterInfo(proposal.id)
        }
        row("Pubkeys that accepted", votingInfo.filter { it.vote }.joinToString { formatProvider(it.pubkey, "") })
        row("Pubkeys that rejected", votingInfo.filterNot { it.vote }.joinToString { formatProvider(it.pubkey, "") })
    }
}

private fun SectionBuilder.printECVotingResults(votingResults: CommonProposalVotingResults) {
    row("Positive votes", votingResults.positiveVotes.toString())
    row("Negative votes", votingResults.negativeVotes.toString())
    row("Max votes", votingResults.maxVotes.toString())
    row("Threshold", formatThreshold(votingResults.threshold))
    row("Status", votingResults.votingResult.toString())
}

private fun SectionBuilder.printECProposalHeader(id: RowId, type: CommonProposalType, timestamp: Long, proposedByPubkey: WrappedByteArray, proposedByName: String) {
    row("Proposal", "${id.id} - ${type.name}")
    row("Proposed by", formatProvider(proposedByPubkey, proposedByName))
    row("Time", "${Date.from(Instant.ofEpochMilli(timestamp))}")
}

private fun formatProvider(providerPubKey: WrappedByteArray, providerName: String) =
        "${providerPubKey.toHex()}${if (providerName.isNotEmpty()) " - $providerName" else ""}"

private fun CliktCommand.formatECPendingProposal(economyChainClient: PostchainClient, proposalId: RowId, proposalType: CommonProposalType): Any {
    when (proposalType) {
        CommonProposalType.ec_tag_create, CommonProposalType.ec_tag_update, CommonProposalType.ec_tag_remove -> {

            val tagCreateProposal = economyChainClient.getTagProposal(proposalId)
            return pmcTable {
                body {
                    row("Name", tagCreateProposal.name)
                    if (tagCreateProposal.scuPrice != -1L) { row("SCU price", tagCreateProposal.scuPrice) }
                    if (tagCreateProposal.extraStoragePrice != -1L) row("Extra storage price", tagCreateProposal.extraStoragePrice)
                }
            }
        }
        CommonProposalType.ec_cluster_create -> {

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

        CommonProposalType.ec_cluster_change_tag -> {

            val clusterChangeTagProposal = economyChainClient.getClusterChangeTagProposal(proposalId)
            return pmcTable {
                body {
                    row("Cluster name", clusterChangeTagProposal.cluster)
                    row("Current tag", clusterChangeTagProposal.currentTag)
                    row("New tag", clusterChangeTagProposal.newTag)
                }
            }
        }

        CommonProposalType.ec_constants_update -> {
            val economyConstantsProposal = economyChainClient.getEcononyConstantsProposal(proposalId)
            return pmcTable {
                body {
                    rowIfNotNull("Min lease time weeks", economyConstantsProposal.minLeaseTimeWeeks)
                    rowIfNotNull("Max lease time weeks", economyConstantsProposal.maxLeaseTimeWeeks)
                    rowIfNotNull("Staking reward fee share", economyConstantsProposal.stakingRewardFeeShare)
                    rowIfNotNull("Chromia foundation fee share", economyConstantsProposal.chromiaFoundationFeeShare)
                    rowIfNotNull("Resource pool margin feee share", economyConstantsProposal.resourcePoolMarginFeeShare)
                    rowIfNotNull("Dapp provider risk share", economyConstantsProposal.dappProviderRiskShare)
                }
            }
        }

        CommonProposalType.ec_voter_set_update -> {
            val proposalDetails = economyChainClient.getEcVoterSetUpdateProposal(proposalId)
            return pmcTable {
                body {
                    rowIfNotNull("Threshold", proposalDetails?.threshold)
                    if (proposalDetails?.addMember != null && proposalDetails.addMember.isNotEmpty()) {
                        row("Add member(s)", proposalDetails.addMember.joinToString(", "))
                    }
                    if (proposalDetails?.removeMember != null && proposalDetails.removeMember.isNotEmpty()) {
                        row("Remove member(s)", proposalDetails.removeMember.joinToString(", "))
                    }
                }
            }
        }

        CommonProposalType.ec_mint -> {
            val proposalDetails = economyChainClient.getMintingProposal(proposalId)
            return pmcTable {
                body {
                    row("Amount", proposalDetails.amount)
                    row("Account ID", proposalDetails.accountId)
                }
            }
        }

        CommonProposalType.ec_system_provider_constants_update -> {
            val proposalDetails = economyChainClient.getSystemProviderEconomyConstantsProposal(proposalId)
            return pmcTable {
                body {
                    rowIfNotNull("Total cost system provider", proposalDetails.totalCostSystemProviders)
                    rowIfNotNull("System provider fee share", proposalDetails.systemProviderFeeShare)
                    rowIfNotNull("System provider risk share", proposalDetails.systemProviderRiskShare)
                }
            }
        }

        CommonProposalType.ec_staking_requirement_constants_update -> {
            val proposalDetails = economyChainClient.getStakingRequirementConstantsProposal(proposalId)
            return pmcTable {
                body {
                    rowIfNotNull("Staking requirements enabled", proposalDetails.enabled)
                    rowIfNotNull("Stop payout days", proposalDetails.stopPayoutDays)
                    rowIfNotNull("Requirement - system provider own staking in chr", proposalDetails.systemProviderOwnStakeChr)
                    rowIfNotNull("Requirement - system provider total staking in chr", proposalDetails.systemProviderTotalStakeChr)
                    rowIfNotNull("Requirement - dapp provider own staking in chr", proposalDetails.dappProviderOwnStakeChr)
                    rowIfNotNull("Requirement - dapp provider total staking in chr", proposalDetails.dappProviderTotalStakeChr)
                }
            }
        }

        else -> return "No details for proposal"
    }
}
