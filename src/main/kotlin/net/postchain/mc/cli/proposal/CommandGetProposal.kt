package net.postchain.mc.cli.proposal

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import de.m3y.kformat.Table
import de.m3y.kformat.table
import net.postchain.chain0.common.queries.getProviderData
import net.postchain.chain0.proposal.GetProposalResult
import net.postchain.chain0.proposal.ProposalState
import net.postchain.chain0.proposal.ProposalType
import net.postchain.chain0.proposal.getProposal
import net.postchain.chain0.proposal.getProposalVoterInfo
import net.postchain.chain0.proposal.getProposalVotingResults
import net.postchain.chain0.proposal_blockchain.getBlockchainActionProposal
import net.postchain.chain0.proposal_blockchain.getBlockchainProposal
import net.postchain.chain0.proposal_blockchain.getConfigurationProposal
import net.postchain.chain0.proposal_blockchain.getConfigurationProposalAt
import net.postchain.chain0.proposal_blockchain_import.getBlockchainImportProposal
import net.postchain.chain0.proposal_blockchain_import.getConfigurationImportProposal
import net.postchain.chain0.proposal_blockchain_import.getFinishBlockchainImportProposal
import net.postchain.chain0.proposal_blockchain_import.getFinishForeignBlockchainImportProposal
import net.postchain.chain0.proposal_blockchain_import.getForeignBlockchainBlocksImportProposal
import net.postchain.chain0.proposal_blockchain_import.getForeignBlockchainImportProposal
import net.postchain.chain0.proposal_cluster.getClusterLimitsProposal
import net.postchain.chain0.proposal_cluster.getClusterProviderProposal
import net.postchain.chain0.proposal_cluster.getClusterRemoveProposal
import net.postchain.chain0.proposal_cluster_anchoring.getClusterAnchoringConfigurationProposal
import net.postchain.chain0.proposal_container.getContainerProposal
import net.postchain.chain0.proposal_container.getContainerRemoveProposal
import net.postchain.chain0.proposal_container.proposal_container_limits.getContainerLimitsProposal
import net.postchain.chain0.proposal_provider.getProviderBatchProposal
import net.postchain.chain0.proposal_provider.getProviderQuotaProposal
import net.postchain.chain0.proposal_provider.getProviderStateProposal
import net.postchain.chain0.proposal_provider.getSystemProviderProposal
import net.postchain.chain0.proposal_voter_set.getVoterSetUpdateProposal
import net.postchain.chain0.version.apiVersion
import net.postchain.client.core.PostchainClient
import net.postchain.common.types.RowId
import net.postchain.common.types.WrappedByteArray
import net.postchain.common.wrap
import net.postchain.crypto.PubKey
import net.postchain.gtv.GtvDecoder
import net.postchain.gtv.GtvDictionary
import net.postchain.gtv.merkle.GtvMerkleHashCalculator
import net.postchain.gtv.merkleHash
import net.postchain.mc.cli.base.ClientUtil
import net.postchain.mc.cli.base.cryptoSystem
import net.postchain.mc.cli.proposal.util.proposalIndexOption
import net.postchain.mc.cli.util.configOption
import net.postchain.mc.cli.votingupdates.formatThreshold
import net.postchain.mc.compatibility.ApiCompatV6.getProposalV6
import net.postchain.mc.gtv.diff.GtvDiffFinder
import java.time.Instant
import java.util.*

class CommandGetProposal : CliktCommand(
        name = "info",
        help = "Gets information of a given proposal"
) {
    private val config by configOption()

    private val id by proposalIndexOption().convert { RowId(it) }

    override fun run() {
        val client = ClientUtil.fromConfig(config)
        val apiVersion = client.apiVersion()
        when {
            apiVersion >= 7 -> {
                val proposal = client.getProposal(id) ?: return echo("Proposal $id not found")
                val proposedBy = client.getProviderData(PubKey(proposal.proposedBy))

                table {
                    row("Proposal:", "${proposal.id.id} - ${proposal.type.name}")
                    row("Proposed by:", "${proposedBy.pubkey.toHex()}${if (proposedBy.name.isNotEmpty()) " - " + proposedBy.name else ""}")
                    row("Time:", "${Date.from(Instant.ofEpochMilli(proposal.timestamp))}")
                    row("State:", proposal.state.toString())
                    printVotingInfo(client, proposal, apiVersion)
                    row("Description:", proposal.description)
                    hints { defaultAlignment = Table.Hints.Alignment.LEFT }
                }.render().also {
                    echo(it.toString())
                }

                if (proposal.state == ProposalState.PENDING) {
                    echo("Proposal details")
                    echo("------------------------------")
                    echo(formatProposal(client, proposal.id, proposal.type))
                }
            }

            else -> {
                val proposal = client.getProposalV6(id) ?: return echo("Proposal $id not found")
                val proposedBy = client.getProviderData(PubKey(proposal.proposedBy))
                val votingResults = client.getProposalVotingResults(proposal.id)

                table {
                    row("Proposal:", "${proposal.id.id} - ${proposal.type.name}")
                    row("Proposed by:", formatProvider(proposedBy.pubkey, proposedBy.name))
                    row("Time:", "${Date.from(Instant.ofEpochMilli(proposal.timestamp))}")
                    row("Positive votes:", votingResults.positiveVotes.toString())
                    row("Negative votes:", votingResults.negativeVotes.toString())
                    row("Max votes:", votingResults.maxVotes.toString())
                    row("Threshold:", formatThreshold(votingResults.threshold))
                    row("Status:", votingResults.votingResult.toString())
                    row("Description:", proposal.description)
                    hints { defaultAlignment = Table.Hints.Alignment.LEFT }
                }.render().also {
                    echo(it.toString())
                }

                echo("Proposal details")
                echo("------------------------------")
                echo(formatProposal(client, proposal.id, proposal.type))
            }
        }
    }

    private fun Table.printVotingInfo(client: PostchainClient, proposal: GetProposalResult, apiVersion: Long) {
        if (proposal.state == ProposalState.PENDING) {
            val votingResults = client.getProposalVotingResults(proposal.id)
            row("Positive votes:", votingResults.positiveVotes.toString())
            row("Negative votes:", votingResults.negativeVotes.toString())
            row("Max votes:", votingResults.maxVotes.toString())
            row("Threshold:", formatThreshold(votingResults.threshold))
            row("Status:", votingResults.votingResult.toString())
        } else if (apiVersion >= 9) {
            val votingInfo = client.getProposalVoterInfo(proposal.id)
            row("Providers that accepted:", votingInfo.filter { it.vote }.joinToString { formatProvider(it.provider, it.providerName) })
            row("Providers that rejected:", votingInfo.filterNot { it.vote }.joinToString { formatProvider(it.provider, it.providerName) })
        }
    }

    private fun formatProvider(providerPubKey: WrappedByteArray, providerName: String) =
            "${providerPubKey.toHex()}${if (providerName.isNotEmpty()) " - $providerName" else ""}"

    private fun formatProposal(client: PostchainClient, proposalId: RowId, proposalType: ProposalType): String {
        return when (proposalType) {
            ProposalType.bc -> {
                val bp = client.getBlockchainProposal(proposalId) ?: return ""
                "Container: ${bp.container}\nConfig hash: ${getDataHash(bp.data)}"
            }

            ProposalType.configuration -> {
                val p = client.getConfigurationProposal(proposalId) ?: return ""
                val currentConf = GtvDecoder.decodeGtv(p.currentConf.data.data) as GtvDictionary
                val newConf = GtvDecoder.decodeGtv(p.proposedConf.data.data) as GtvDictionary
                "Proposed configuration:\n\n${GtvDiffFinder.diff(currentConf, newConf).diff}"
            }

            ProposalType.configuration_at -> {
                val p = client.getConfigurationProposalAt(proposalId) ?: return ""
                val currentConf = GtvDecoder.decodeGtv(p.currentConf.data.data) as GtvDictionary
                val newConf = GtvDecoder.decodeGtv(p.proposedConf.data.data) as GtvDictionary
                "Enabled at height: ${p.proposedConf.height}\n\n${GtvDiffFinder.diff(currentConf, newConf).diff}"
            }

            ProposalType.voter_set_update -> {
                val vsu = client.getVoterSetUpdateProposal(proposalId.id) ?: return ""
                val t = table {
                    row("Voter set:", vsu.voterSet)
                    row("Governor update:", vsu.governor ?: "")
                    row("Majority threshold update:", vsu.threshold?.toString() ?: "")
                    row("New member:", vsu.addMember.joinToString(", ") { it.toHex() })
                    row("Remove member:", vsu.removeMember.joinToString(", ") { it.toHex() })
                    hints { defaultAlignment = Table.Hints.Alignment.LEFT }
                }.render()
                return t.toString()
            }

            ProposalType.cluster_provider -> {
                val cpc = client.getClusterProviderProposal(proposalId) ?: return ""
                return table {
                    row("Cluster:", cpc.cluster)
                    row("Provider:", cpc.provider.toHex())
                    row("Add/Remove:", if (cpc.add) "Add" else "remove")
                    hints { defaultAlignment = Table.Hints.Alignment.LEFT }
                }.render().toString()
            }

            ProposalType.provider_is_system -> {
                val pis = client.getSystemProviderProposal(proposalId) ?: return ""
                return table {
                    row("Provider:", pis.provider.toHex())
                    row("Add/Remove:", if (pis.add) "Add" else "remove")
                    hints { defaultAlignment = Table.Hints.Alignment.LEFT }
                }.render().toString()
            }

            ProposalType.provider_quota -> {
                val ppq = client.getProviderQuotaProposal(proposalId) ?: return ""
                return table {
                    row("Provider tier:", ppq.tier.name)
                    row("Quota type:", ppq.quotaType.name)
                    row("Value:", ppq.value.toString())
                    hints { defaultAlignment = Table.Hints.Alignment.LEFT }
                }.render().toString()
            }

            ProposalType.provider_batch -> {
                val ppb = client.getProviderBatchProposal(proposalId) ?: return ""

                val info = table {
                    row("Provider tier:", ppb.tier.toString())
                    row("System:", ppb.system.toString())
                    row("Active:", ppb.active.toString())
                    hints { defaultAlignment = Table.Hints.Alignment.LEFT }
                }.render().toString()

                val providers = table {
                    header("Pubkey", "Name", "Url")
                    ppb.providerInfos.forEach {
                        row(it.pubkey.toString(), it.name, it.url)
                    }
                    hints { defaultAlignment = Table.Hints.Alignment.LEFT }
                }.render().toString()

                return info + providers
            }

            ProposalType.container_limits -> {
                val pcl = client.getContainerLimitsProposal(proposalId) ?: return ""
                return table {
                    row("Container:", pcl.container)
                    row("Container Units:", pcl.containerUnits.toString())
                    row("Max blockchains:", pcl.maxBlockchains.toString())
                    hints { defaultAlignment = Table.Hints.Alignment.LEFT }
                }.render().toString()
            }

            ProposalType.cluster_limits -> {
                val pcl = client.getClusterLimitsProposal(proposalId) ?: return ""
                return table {
                    row("Cluster:", pcl.cluster)
                    row("Cluster Units:", pcl.clusterUnits.toString())
                    hints { defaultAlignment = Table.Hints.Alignment.LEFT }
                }.render().toString()
            }

            ProposalType.cluster_remove -> {
                val cluster = client.getClusterRemoveProposal(proposalId) ?: return ""
                return "Cluster to remove: $cluster"
            }

            ProposalType.provider_state -> {
                val pps = client.getProviderStateProposal(proposalId) ?: return ""
                return table {
                    row("Provider:", pps.provider.toHex())
                    row("Provider name:", pps.providerName)
                    row("Enable/Disable:", if (pps.active) "Enable" else "Disable")
                    hints { defaultAlignment = Table.Hints.Alignment.LEFT }
                }.render().toString()
            }

            ProposalType.blockchain_action -> {
                val pba = client.getBlockchainActionProposal(proposalId) ?: return ""
                return table {
                    row("Blockchain:", pba.blockchain.toHex())
                    row("Blockchain name:", pba.blockchainName)
                    row("Action:", pba.action.name)
                    hints { defaultAlignment = Table.Hints.Alignment.LEFT }
                }.render().toString()
            }

            ProposalType.cluster_anchoring_configuration -> {
                val p = client.getClusterAnchoringConfigurationProposal(proposalId) ?: return ""
                val currentConf = GtvDecoder.decodeGtv(p.currentConf.data) as GtvDictionary
                val newConf = GtvDecoder.decodeGtv(p.proposedConf.data) as GtvDictionary
                "Proposed anchoring configuration:\n\n${GtvDiffFinder.diff(currentConf, newConf).diff}"
            }

            ProposalType.container -> {
                val pc = client.getContainerProposal(proposalId) ?: return ""
                return table {
                    row("Container:", pc.container)
                    row("Container Units:", pc.containerUnits.toString())
                    row("Max blockchains:", pc.maxBlockchains.toString())
                    hints { defaultAlignment = Table.Hints.Alignment.LEFT }
                }.render().toString()
            }

            ProposalType.container_remove -> {
                val container = client.getContainerRemoveProposal(proposalId) ?: return ""
                return "Container to remove: $container"
            }

            ProposalType.blockchain_import -> {
                val bip = client.getBlockchainImportProposal(proposalId) ?: return ""
                val conf = GtvDecoder.decodeGtv(bip.configData.data)
                return "Blockchain RID:\n${bip.blockchainRid}\n\nName: ${bip.name}\nContainer: ${bip.container}\nConfig hash: ${getDataHash(bip.configData)}"
            }

            ProposalType.configuration_import -> {
                val cip = client.getConfigurationImportProposal(proposalId) ?: return ""
                return "Blockchain RID:\n${cip.blockchainRid}\nHeight: ${cip.height}\nConfig hash: ${getDataHash(cip.configData)}"
            }

            ProposalType.finish_blockchain_import -> {
                val fbi = client.getFinishBlockchainImportProposal(proposalId) ?: return ""
                return "Blockchain RID: ${fbi.blockchainRid}"
            }

            ProposalType.foreign_blockchain_import -> {
                val proposal = client.getForeignBlockchainImportProposal(proposalId) ?: return ""
                return "$proposal"
            }

            ProposalType.foreign_blockchain_blocks_import -> {
                val proposal = client.getForeignBlockchainBlocksImportProposal(proposalId) ?: return ""
                return "$proposal"
            }

            ProposalType.finish_foreign_blockchain_import -> {
                val proposal = client.getFinishForeignBlockchainImportProposal(proposalId) ?: return ""
                return "$proposal"
            }
        }
    }

    private fun getDataHash(configData: WrappedByteArray) = GtvDecoder.decodeGtv(configData.data)
            .merkleHash(GtvMerkleHashCalculator(cryptoSystem))
            .wrap()
}
