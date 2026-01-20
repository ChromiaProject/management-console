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
import net.postchain.economy.common_proposal.CommonProposalData
import net.postchain.economy.common_proposal.CommonProposalState
import net.postchain.economy.common_proposal.CommonProposalType
import net.postchain.economy.common_proposal.CommonProposalVotingResults
import net.postchain.economy.common_proposal.getCommonProposal
import net.postchain.economy.common_proposal.getCommonProposalVoterInfo
import net.postchain.economy.common_proposal.getCommonProposalVotingResults
import net.postchain.economy.economy_chain.getClusterChangeTagProposal
import net.postchain.economy.economy_chain.getClusterCreateProposal
import net.postchain.economy.economy_chain.getEcononyConstantsProposal
import net.postchain.economy.economy_chain.getMintingProposal
import net.postchain.economy.economy_chain.getPriceOracleRateProposal
import net.postchain.economy.economy_chain.getStakingRequirementConstantsProposal
import net.postchain.economy.economy_chain.getSystemProviderEconomyConstantsProposal
import net.postchain.economy.economy_chain.getTagProposal
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.ECONOMY_CHAIN_DYNAMIC_STAKING_REWARD_SHARE_VERSION
import net.postchain.mc.cli.base.rowIfNotNull
import net.postchain.mc.cli.economy.formatChr
import net.postchain.mc.cli.economy.formatUsd
import net.postchain.mc.cli.proposal.util.proposalIndexOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.cli.votingupdates.formatThreshold
import net.postchain.mc.compatibility.ApiCompatECV63.getEcononyConstantsProposalECV63
import net.postchain.token.common_proposal.voter_set_proposal.getVoterSetUpdateProposal
import java.time.Instant
import java.util.Date

class CommandGetProposal : ECBaseCommand(
        name = "info",
        help = "Gets information of a given proposal"
) {
    private val id by proposalIndexOption().convert { RowId(it) }

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        showECProposalInfo(client, economyChainClient, id, ecVersion.version)
    }
}

fun CliktCommand.showECProposalInfo(client: PostchainClient, economyChainClient: PostchainClient, id: RowId?, ecVersion: Long) {

    val proposal = economyChainClient.getCommonProposal(id) ?: return echo("Proposal $id not found")
    val proposedBy = client.getProviderData(PubKey(proposal.proposedBy))

    echo(pmcTable {
        body {
            printECProposalHeader(proposal.id, proposal.type, proposal.timestamp, proposedBy.pubkey, proposedBy.name)
            row("State", proposal.state.toString())
            proposal.applyAt?.let {
                row("Apply at", "${Date.from(Instant.ofEpochMilli(it))}")
            }
            proposal.scheduledAt?.let {
                row("Scheduled at", "${Date.from(Instant.ofEpochMilli(it))}")
            }
            printECVotingInfo(economyChainClient, proposal)
            row("Description", proposal.description)
        }
    })

    if (proposal.state == CommonProposalState.PENDING) {
        if (terminal.terminalInfo.outputInteractive) echo("Proposal details")
        echo(formatECPendingProposal(economyChainClient, proposal.id, proposal.type, ecVersion))
    }
}

private fun SectionBuilder.printECVotingInfo(economyChainClient: PostchainClient, proposal: CommonProposalData) {
    if (proposal.state == CommonProposalState.PENDING) {
        val votingResults = economyChainClient.getCommonProposalVotingResults(proposal.id)
        printECVotingResults(votingResults)
    } else {
        val votingInfo = economyChainClient.getCommonProposalVoterInfo(proposal.id)
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

private fun CliktCommand.formatECPendingProposal(economyChainClient: PostchainClient, proposalId: RowId, proposalType: CommonProposalType, ecVersion: Long): Any {
    when (proposalType) {
        CommonProposalType.ec_tag_create, CommonProposalType.ec_tag_update, CommonProposalType.ec_tag_remove -> {

            val tagCreateProposal = economyChainClient.getTagProposal(proposalId)

            return pmcTable {
                body {
                    row("Name", tagCreateProposal.name)
                    if (tagCreateProposal.scuPrice != -1L) {
                        row("SCU price USD", formatUsd(tagCreateProposal.scuPrice))
                    }
                    if (tagCreateProposal.extraStoragePrice != -1L) {
                        row("Extra storage price USD", formatUsd(tagCreateProposal.extraStoragePrice))
                    }
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
                    row("Container unit - CPU", clusterCreateProposal.containerUnitCpu)
                    row("Container unit - RAM", clusterCreateProposal.containerUnitRam)
                    row("Container unit - storage", clusterCreateProposal.containerUnitStorage)
                    row("Container unit - I/O read", clusterCreateProposal.containerUnitIoRead)
                    row("Container unit - I/O write", clusterCreateProposal.containerUnitIoWrite)
                    row("System container units", clusterCreateProposal.systemContainerUnits)
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
            if (ecVersion < ECONOMY_CHAIN_DYNAMIC_STAKING_REWARD_SHARE_VERSION) {
                val economyConstantsProposal = economyChainClient.getEcononyConstantsProposalECV63(proposalId)
                return pmcTable {
                    body {
                        rowIfNotNull("Min lease time weeks", economyConstantsProposal.minLeaseTimeWeeks)
                        rowIfNotNull("Max lease time weeks", economyConstantsProposal.maxLeaseTimeWeeks)
                        rowIfNotNull("Staking reward rate", economyConstantsProposal.stakingRewardRate)
                        rowIfNotNull("Staking reward fee share", economyConstantsProposal.stakingRewardFeeShare)
                        rowIfNotNull("Chromia foundation fee share", economyConstantsProposal.chromiaFoundationFeeShare)
                        rowIfNotNull("Resource pool margin fee share", economyConstantsProposal.resourcePoolMarginFeeShare)
                        rowIfNotNull("Dapp provider risk share", economyConstantsProposal.dappProviderRiskShare)
                    }
                }
            } else {
                val economyConstantsProposal = economyChainClient.getEcononyConstantsProposal(proposalId)
                return pmcTable {
                    body {
                        rowIfNotNull("Min lease time weeks", economyConstantsProposal.minLeaseTimeWeeks)
                        rowIfNotNull("Max lease time weeks", economyConstantsProposal.maxLeaseTimeWeeks)
                        rowIfNotNull("Staking reward rate", economyConstantsProposal.stakingRewardRate)
                        rowIfNotNull("Chromia foundation fee share", economyConstantsProposal.chromiaFoundationFeeShare)
                        rowIfNotNull("Resource pool margin fee share", economyConstantsProposal.resourcePoolMarginFeeShare)
                        rowIfNotNull("Dapp provider risk share", economyConstantsProposal.dappProviderRiskShare)
                    }
                }
            }
        }

        CommonProposalType.common_voter_set_update -> {
            val proposalDetails = economyChainClient.getVoterSetUpdateProposal(proposalId)
            return pmcTable {
                body {
                    rowIfNotNull("Threshold", proposalDetails.threshold)
                    if (!proposalDetails.addMember.isNullOrEmpty()) {
                        row("Add member(s)", proposalDetails.addMember.joinToString(", "))
                    }
                    if (!proposalDetails.removeMember.isNullOrEmpty()) {
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
                    proposalDetails.totalCostSystemProviders?.let { row("Total cost system provider in USD", formatUsd(it)) }
                    rowIfNotNull("System provider fee share", proposalDetails.systemProviderFeeShare)
                    rowIfNotNull("System provider risk share", proposalDetails.systemProviderRiskShare)
                }
            }
        }

        CommonProposalType.ec_staking_requirement_constants_update -> {
            val proposalDetails = economyChainClient.getStakingRequirementConstantsProposal(proposalId)
            return pmcTable {
                body {
                    proposalDetails.enabled?.let { row("Staking requirements enabled", it) }
                    proposalDetails.stopPayoutDays?.let { row("Stop payout days", it) }
                    proposalDetails.systemNodeOwnStakeChr?.let { row("Requirement - system node own staking in CHR", formatChr(it)) }
                    proposalDetails.systemNodeTotalStakeChr?.let { row("Requirement - system node total staking in CHR", formatChr(it)) }
                    proposalDetails.dappNodeOwnStakeChr?.let { row("Requirement - dapp node own staking in CHR", formatChr(it)) }
                    proposalDetails.dappNodeTotalStakeChr?.let { row("Requirement - dapp node total staking in CHR", formatChr(it)) }
                }
            }
        }

        CommonProposalType.ec_price_oracle_rate -> {
            val proposalDetails = economyChainClient.getPriceOracleRateProposal(proposalId)
            return pmcTable {
                body {
                    for (proposalDetail in proposalDetails) {
                        row("%s price:".format(proposalDetail.symbol), proposalDetail.price)
                        if (proposalDetail.name != null) {
                            row("%s name:".format(proposalDetail.symbol), proposalDetail.name)
                        }
                    }
                }
            }
        }

        else -> return "No details for proposal"
    }
}
