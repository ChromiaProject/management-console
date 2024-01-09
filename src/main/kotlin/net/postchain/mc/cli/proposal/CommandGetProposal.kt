package net.postchain.mc.cli.proposal

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.table.SectionBuilder
import net.postchain.chain0.common.queries.getProviderData
import net.postchain.chain0.proposal.GetProposalResult
import net.postchain.chain0.proposal.ProposalState
import net.postchain.chain0.proposal.ProposalType
import net.postchain.chain0.proposal.ProposalVotingResults
import net.postchain.chain0.proposal.getBlockchainConfigurationUpdateAttemptStateByProposal
import net.postchain.chain0.proposal.getProposal
import net.postchain.chain0.proposal.getProposalVoterInfo
import net.postchain.chain0.proposal.getProposalVotingResults
import net.postchain.chain0.proposal_blockchain.BlockchainAction
import net.postchain.chain0.proposal_blockchain.getBlockchainActionProposal
import net.postchain.chain0.proposal_blockchain.getBlockchainProposal
import net.postchain.chain0.proposal_blockchain.getBlockchainUnarchiveActionProposal
import net.postchain.chain0.proposal_blockchain.getConfigurationProposal
import net.postchain.chain0.proposal_blockchain.getConfigurationProposalAt
import net.postchain.chain0.proposal_blockchain_import.getBlockchainImportProposal
import net.postchain.chain0.proposal_blockchain_import.getConfigurationImportProposal
import net.postchain.chain0.proposal_blockchain_import.getFinishBlockchainImportProposal
import net.postchain.chain0.proposal_blockchain_import.getForeignBlockchainBlocksImportProposal
import net.postchain.chain0.proposal_blockchain_import.getForeignBlockchainImportProposal
import net.postchain.chain0.proposal_blockchain_move.getBlockchainMoveFinishProposal
import net.postchain.chain0.proposal_blockchain_move.getBlockchainMoveProposal
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
import net.postchain.gtv.GtvFactory
import net.postchain.gtv.merkle.GtvMerkleHashCalculator
import net.postchain.gtv.merkleHash
import net.postchain.mc.cli.base.cryptoSystem
import net.postchain.mc.cli.proposal.util.proposalIndexOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.votingupdates.formatThreshold
import net.postchain.mc.compatibility.ApiCompatV22.getClusterLimitsProposalV22
import net.postchain.mc.compatibility.ApiCompatV22.getContainerLimitsProposalV22
import net.postchain.mc.compatibility.ApiCompatV22.getContainerProposalV22
import net.postchain.mc.compatibility.ApiCompatV6.getProposalV6
import net.postchain.mc.gtv.diff.GtvDiffFinder
import java.time.Instant
import java.util.Date

private val AUTO_GENERATED_CONFIG_FIELDS = setOf(
        "chain0_last_block_rid",
        "chain0_block_height",
        "chain0_tx_rid",
        "chain0_op_index"
)

class CommandGetProposal : CliktCommand(
        name = "info",
        help = "Gets information of a given proposal"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val id by proposalIndexOption().convert { RowId(it) }

    override fun run() {
        showProposalInfo(client, id)
    }
}

fun CliktCommand.showProposalInfo(client: PostchainClient, id: RowId?) {
    val apiVersion = client.apiVersion()
    when {
        apiVersion >= 7 -> {
            val proposal = client.getProposal(id) ?: return echo("Proposal $id not found")
            val proposedBy = client.getProviderData(PubKey(proposal.proposedBy))

            echo(defaultTable {
                body {
                    printProposalHeader(proposal.id, proposal.type, proposal.timestamp, proposedBy.pubkey, proposedBy.name)
                    row("State", proposal.state.toString())
                    printVotingInfo(client, proposal, apiVersion)
                    row("Description", proposal.description)
                }
            })

            if (proposal.state == ProposalState.PENDING) {
                echo("Proposal details")
                echo(formatPendingProposal(apiVersion, client, proposal.id, proposal.type))
            } else if (apiVersion >= 26 && proposal.state == ProposalState.APPROVED && proposal.type == ProposalType.configuration) {
                printApprovedConfigurationStatus(client, proposal)
            }
        }

        else -> {
            val proposal = client.getProposalV6(id) ?: return echo("Proposal $id not found")
            val proposedBy = client.getProviderData(PubKey(proposal.proposedBy))

            echo(defaultTable {
                body {
                    printProposalHeader(proposal.id, proposal.type, proposal.timestamp, proposedBy.pubkey, proposedBy.name)
                    printVotingResults(client.getProposalVotingResults(proposal.id))
                    row("Description", proposal.description)
                }
            })

            echo("Proposal details")
            echo(formatPendingProposal(apiVersion, client, proposal.id, proposal.type))
        }
    }
}

private fun CliktCommand.printApprovedConfigurationStatus(client: PostchainClient, proposal: GetProposalResult) {
    val updateState = client.getBlockchainConfigurationUpdateAttemptStateByProposal(proposal.id)
    if (updateState != null) {
        val heightInfo = if (updateState.appliedAtHeight > -1) updateState.appliedAtHeight.toString() else "Not applied yet"
        echo(defaultTable {
            captionTop("Configuration update status", TextAlign.LEFT)
            header { row("Status", "Applied at height") }
            body {
                row(updateState.state.toString(), heightInfo)
            }
        })
    }
}

private fun SectionBuilder.printVotingInfo(client: PostchainClient, proposal: GetProposalResult, apiVersion: Long) {
    if (proposal.state == ProposalState.PENDING) {
        printVotingResults(client.getProposalVotingResults(proposal.id))
    } else if (apiVersion >= 9) {
        val votingInfo = client.getProposalVoterInfo(proposal.id)
        row("Providers that accepted", votingInfo.filter { it.vote }.joinToString { formatProvider(it.provider, it.providerName) })
        row("Providers that rejected", votingInfo.filterNot { it.vote }.joinToString { formatProvider(it.provider, it.providerName) })
    }
}

private fun SectionBuilder.printVotingResults(votingResults: ProposalVotingResults) {
    row("Positive votes", votingResults.positiveVotes.toString())
    row("Negative votes", votingResults.negativeVotes.toString())
    row("Max votes", votingResults.maxVotes.toString())
    row("Threshold", formatThreshold(votingResults.threshold))
    row("Status", votingResults.votingResult.toString())
}

private fun SectionBuilder.printProposalHeader(id: RowId, type: ProposalType, timestamp: Long, proposedByPubkey: WrappedByteArray, proposedByName: String) {
    row("Proposal", "${id.id} - ${type.name}")
    row("Proposed by", formatProvider(proposedByPubkey, proposedByName))
    row("Time", "${Date.from(Instant.ofEpochMilli(timestamp))}")
}

private fun formatProvider(providerPubKey: WrappedByteArray, providerName: String) =
        "${providerPubKey.toHex()}${if (providerName.isNotEmpty()) " - $providerName" else ""}"

private fun CliktCommand.formatPendingProposal(apiVersion: Long, client: PostchainClient, proposalId: RowId, proposalType: ProposalType): Any {
    return when (proposalType) {
        ProposalType.bc -> {
            val bp = client.getBlockchainProposal(proposalId) ?: return ""
            "Container: ${bp.container}\nConfig hash: ${getDataHash(bp.data)}"
        }

        ProposalType.configuration -> {
            val p = client.getConfigurationProposal(proposalId) ?: return ""
            val currentConf = pruneAutoGeneratedConfigFields(GtvDecoder.decodeGtv(p.currentConf.data.data) as GtvDictionary)
            val newConf = GtvDecoder.decodeGtv(p.proposedConf.data.data) as GtvDictionary
            "Proposed configuration:\n\n${GtvDiffFinder.diff(currentConf, newConf).diff}"
        }

        ProposalType.configuration_at -> {
            val p = client.getConfigurationProposalAt(proposalId) ?: return ""
            val currentConf = pruneAutoGeneratedConfigFields(GtvDecoder.decodeGtv(p.currentConf.data.data) as GtvDictionary)
            val newConf = GtvDecoder.decodeGtv(p.proposedConf.data.data) as GtvDictionary
            "Enabled at height: ${p.proposedConf.height}\n\n${GtvDiffFinder.diff(currentConf, newConf).diff}"
        }

        ProposalType.voter_set_update -> {
            val vsu = client.getVoterSetUpdateProposal(proposalId.id) ?: return ""
            return defaultTable {
                body {
                    row("Voter set", vsu.voterSet)
                    row("Governor update", vsu.governor ?: "")
                    row("Majority threshold update", vsu.threshold?.toString() ?: "")
                    row("New member", vsu.addMember.joinToString(", ") { it.toHex() })
                    row("Remove member", vsu.removeMember.joinToString(", ") { it.toHex() })
                }
            }
        }

        ProposalType.cluster_provider -> {
            val cpc = client.getClusterProviderProposal(proposalId) ?: return ""
            return defaultTable {
                body {
                    row("Cluster", cpc.cluster)
                    row("Provider", cpc.provider.toHex())
                    row("Add/Remove", if (cpc.add) "Add" else "remove")
                }
            }
        }

        ProposalType.provider_is_system -> {
            val pis = client.getSystemProviderProposal(proposalId) ?: return ""
            return defaultTable {
                body {
                    row("Provider", pis.provider.toHex())
                    row("Add/Remove", if (pis.add) "Add" else "remove")
                }
            }
        }

        ProposalType.provider_quota -> {
            val ppq = client.getProviderQuotaProposal(proposalId) ?: return ""
            return defaultTable {
                body {
                    row("Provider tier", ppq.tier.name)
                    row("Quota type", ppq.quotaType.name)
                    row("Value", ppq.value.toString())
                }
            }
        }

        ProposalType.provider_batch -> {
            val ppb = client.getProviderBatchProposal(proposalId) ?: return ""

            echo(defaultTable {
                body {
                    row("Provider tier", ppb.tier.toString())
                    row("System", ppb.system.toString())
                    row("Active", ppb.active.toString())
                }
            })

            val providers = defaultTable {
                header { row("Pubkey", "Name", "Url") }
                body {

                    ppb.providerInfos.forEach {
                        row(it.pubkey.toString(), it.name, it.url)
                    }
                }
            }

            return providers
        }

        ProposalType.container_limits -> {
            when {
                apiVersion >= 24 -> {
                    val pcl = client.getContainerLimitsProposal(proposalId) ?: return ""
                    return defaultTable {
                        body {
                            row("Container", pcl.container)
                            row("Container Units", pcl.containerUnits.toString())
                            row("Max blockchains", pcl.maxBlockchains.toString())
                            row("Extra Storage", pcl.extraStorage.toString())
                        }
                    }
                }

                else -> {
                    val pcl = client.getContainerLimitsProposalV22(proposalId) ?: return ""
                    return defaultTable {
                        body {
                            row("Container", pcl.container)
                            row("Container Units", pcl.containerUnits.toString())
                            row("Max blockchains", pcl.maxBlockchains.toString())
                        }
                    }
                }
            }
        }

        ProposalType.cluster_limits -> {
            when {
                apiVersion >= 24 -> {
                    val pcl = client.getClusterLimitsProposal(proposalId) ?: return ""
                    return defaultTable {
                        body {
                            row("Cluster", pcl.cluster)
                            row("Cluster Units", pcl.clusterUnits.toString())
                            row("Extra Storage", pcl.extraStorage.toString())
                        }
                    }
                }

                else -> {
                    val pcl = client.getClusterLimitsProposalV22(proposalId) ?: return ""
                    return defaultTable {
                        body {
                            row("Cluster", pcl.cluster)
                            row("Cluster Units", pcl.clusterUnits.toString())
                        }
                    }
                }
            }
        }

        ProposalType.cluster_remove -> {
            val cluster = client.getClusterRemoveProposal(proposalId) ?: return ""
            return "Cluster to remove: $cluster"
        }

        ProposalType.provider_state -> {
            val pps = client.getProviderStateProposal(proposalId) ?: return ""
            return defaultTable {
                body {
                    row("Provider", pps.provider.toHex())
                    row("Provider name", pps.providerName)
                    row("Enable/Disable", if (pps.active) "Enable" else "Disable")
                }
            }
        }

        ProposalType.blockchain_action -> {
            val pba = client.getBlockchainActionProposal(proposalId) ?: return ""
            return defaultTable {
                body {
                    row("Blockchain RID", pba.blockchain.toHex())
                    row("Blockchain name", pba.blockchainName)
                    row("Action", pba.action.name)

                    if (apiVersion >= 28 && pba.action == BlockchainAction.unarchive) {
                        val unarchivingProposal = client.getBlockchainUnarchiveActionProposal(proposalId)
                        if (unarchivingProposal != null) {
                            row("Source container", unarchivingProposal.sourceContainer)
                            row("Destination container", unarchivingProposal.destinationContainer)
                            row("Up to height", unarchivingProposal.upToHeight)
                        }
                    }
                }
            }
        }

        ProposalType.cluster_anchoring_configuration -> {
            val p = client.getClusterAnchoringConfigurationProposal(proposalId) ?: return ""
            val currentConf = pruneAutoGeneratedConfigFields(GtvDecoder.decodeGtv(p.currentConf.data) as GtvDictionary)
            val newConf = GtvDecoder.decodeGtv(p.proposedConf.data) as GtvDictionary
            "Proposed anchoring configuration:\n\n${GtvDiffFinder.diff(currentConf, newConf).diff}"
        }

        ProposalType.container -> {
            when {
                apiVersion >= 24 -> {
                    val pc = client.getContainerProposal(proposalId) ?: return ""
                    return defaultTable {
                        body {
                            row("Container", pc.container)
                            row("Container Units", pc.containerUnits.toString())
                            row("Max blockchains", pc.maxBlockchains.toString())
                            row("Extra Storage", pc.extraStorage.toString())
                        }
                    }
                }

                else -> {
                    val pc = client.getContainerProposalV22(proposalId) ?: return ""
                    return defaultTable {
                        body {
                            row("Container", pc.container)
                            row("Container Units", pc.containerUnits.toString())
                            row("Max blockchains", pc.maxBlockchains.toString())
                        }
                    }
                }
            }
        }

        ProposalType.container_remove -> {
            val container = client.getContainerRemoveProposal(proposalId) ?: return ""
            return "Container to remove: $container"
        }

        ProposalType.blockchain_import -> {
            val proposal = client.getBlockchainImportProposal(proposalId) ?: return ""
            return defaultTable {
                body {
                    row("Blockchain RID", proposal.blockchainRid)
                    row("Blockchain Name", proposal.name)
                    row("Container", proposal.container)
                    row("Config hash", getDataHash(proposal.configData))
                }
            }
        }

        ProposalType.configuration_import -> {
            val proposal = client.getConfigurationImportProposal(proposalId) ?: return ""
            return defaultTable {
                body {
                    row("Blockchain RID", proposal.blockchainRid)
                    row("Height", proposal.height)
                    row("Config hash", getDataHash(proposal.configData))
                }
            }
        }

        ProposalType.finish_blockchain_import -> {
            val proposal = client.getFinishBlockchainImportProposal(proposalId) ?: return ""
            return defaultTable {
                body {
                    row("Blockchain RID", proposal.blockchainRid)
                    row("Finish at height", proposal.finishAtHeight)
                }
            }
        }

        ProposalType.foreign_blockchain_import -> {
            val proposal = client.getForeignBlockchainImportProposal(proposalId) ?: return ""
            return defaultTable {
                body {
                    row("Foreign node", proposal.foreignNode)
                    row("Host", proposal.host)
                    row("Port", proposal.port)
                    row("Api url", proposal.apiUrl)
                    row("Chain0 RID", proposal.chain0Rid)
                    row("Blockchain name", proposal.blockchainName)
                    row("Blockchain RID", proposal.blockchainRid)
                    row("Container", proposal.container)
                }
            }
        }

        ProposalType.foreign_blockchain_blocks_import -> {
            val proposal = client.getForeignBlockchainBlocksImportProposal(proposalId) ?: return ""
            return defaultTable {
                body {
                    row("Blockchain RID", proposal.blockchainRid)
                    row("Up to height", proposal.upToHeight)
                }
            }
        }

        ProposalType.blockchain_move_start -> {
            val proposal = client.getBlockchainMoveProposal(proposalId) ?: return ""
            return defaultTable {
                body {
                    row("Blockchain RID", proposal.blockchainRid)
                    row("Blockchain name", proposal.blockchainName)
                    row("Cluster", proposal.cluster)
                    row("Container", proposal.container)
                }
            }
        }

        ProposalType.blockchain_move_cancel -> {
//            val proposal = client.getBlockchainMoveCancelProposal(proposalId) ?: return ""
//            return "$proposal"
            return "Not yet implemented"
        }

        ProposalType.blockchain_move_finish -> {
            val proposal = client.getBlockchainMoveFinishProposal(proposalId) ?: return ""
            return defaultTable {
                body {
                    row("Blockchain RID", proposal.blockchainRid)
                    row("Blockchain name", proposal.blockchainName)
                    row("Cluster", proposal.cluster)
                    row("Container", proposal.container)
                    row("Finish at height", proposal.finishAtHeight)
                }
            }
        }
    }
}

private fun getDataHash(configData: WrappedByteArray) = GtvDecoder.decodeGtv(configData.data)
        .merkleHash(GtvMerkleHashCalculator(cryptoSystem))
        .wrap()

private fun pruneAutoGeneratedConfigFields(config: GtvDictionary): GtvDictionary =
        GtvFactory.gtv(config.asDict().filterKeys { !AUTO_GENERATED_CONFIG_FIELDS.contains(it) })
