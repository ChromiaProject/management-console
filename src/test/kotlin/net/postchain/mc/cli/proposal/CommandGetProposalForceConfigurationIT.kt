package net.postchain.mc.cli.proposal

import net.postchain.chain0.proposal.ProposalData
import net.postchain.chain0.proposal.ProposalState
import net.postchain.chain0.proposal.ProposalType
import net.postchain.chain0.proposal.ProposalVotingResults
import net.postchain.chain0.proposal.voting.VotingResult
import net.postchain.chain0.proposal_blockchain.BlockchainConfigurationData
import net.postchain.chain0.proposal_blockchain.ForcedConfigurationProposalData
import net.postchain.chain0.proposal_blockchain.GET_FORCED_CONFIGURATION_PROPOSAL_V64
import net.postchain.chain0.proposal_blockchain.GET_PROPOSED_FORCED_CONFIGURATION
import net.postchain.chain0.proposal_blockchain.GetProposedForcedConfigurationResult
import net.postchain.chain0.proposal_blockchain.ProposedForcedConfigurationData
import net.postchain.common.hexStringToByteArray
import net.postchain.common.types.RowId
import net.postchain.common.types.WrappedByteArray
import net.postchain.common.wrap
import net.postchain.gtv.GtvEncoder
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.provider.addDcGetProviderData
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandGetProposalForceConfigurationIT {

    private val proposalId = RowId(123L)
    private val blockchainRid = WrappedByteArray("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF".hexStringToByteArray())
    private val proposedBy = DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray().wrap()
    
    @Test
    fun `force configuration proposal with API version 64 - shows blockchain RID, height, resume-chain and diff`(@TempDir dir: Path) {
        val currentConfig = gtv(mapOf("foo" to gtv(17), "bar" to gtv("old_value")))
        val proposedConfig = gtv(mapOf("foo" to gtv(23), "bar" to gtv("old_value"), "baz" to gtv("new_value")))
        
        ManagedRestTestApi(dir, dcVersion = 64)
                .withDCQuery("get_proposal", GtvObjectMapper.toGtvDictionary(ProposalData(
                        id = proposalId,
                        type = ProposalType.force_configuration,
                        proposedBy = proposedBy,
                        timestamp = 1234567890000L,
                        description = "Force configuration test",
                        state = ProposalState.PENDING,
                        applyAt = null,
                        scheduledAt = null,
                        txRid = WrappedByteArray(32),
                        voterSetName = null
                )))
                .withDCQuery("get_proposal_voting_results", GtvObjectMapper.toGtvDictionary(ProposalVotingResults(
                        positiveVotes = 1,
                        negativeVotes = 0,
                        maxVotes = 3,
                        threshold = 2,
                        votingResult = VotingResult.pending,
                        voterSetName = null
                )))
                .withDCQuery("get_proposal_voter_info", gtv(listOf()))
                .withDCQuery(GET_FORCED_CONFIGURATION_PROPOSAL_V64, GtvObjectMapper.toGtvDictionary(
                        ForcedConfigurationProposalData(
                                blockchainRid = blockchainRid,
                                currentHeight = 17,
                                currentConfiguration = GtvEncoder.encodeGtv(currentConfig).wrap(),
                                proposedHeight = 100,
                                proposedConfiguration = GtvEncoder.encodeGtv(proposedConfig).wrap(),
                                resumeChain = true
                        )
                ))
                .addDcGetProviderData()
                .testCommand(
                        CommandGetProposal(),
                        "--id", proposalId.id.toString()
                ) { result, _ ->
                    assertCommandSuccessContains(result, "Force for blockchain ${blockchainRid.toHex()} at height: 100")
                    assertCommandSuccessContains(result, "Resume-chain: true")
                    assertCommandSuccessContains(result, "Path: foo")
                    assertCommandSuccessContains(result, "Value changed from 17 to 23")
                    assertCommandSuccessContains(result, "Path: baz")
                    assertCommandSuccessContains(result, "baz was added:")
                    assertCommandSuccessContains(result, "new_value")
                }
    }

    @Test
    fun `force configuration proposal with API version 64 - resume chain false`(@TempDir dir: Path) {
        val currentConfig = gtv(mapOf("foo" to gtv(17)))
        val proposedConfig = gtv(mapOf("foo" to gtv(23)))
        
        ManagedRestTestApi(dir, dcVersion = 64)
                .withDCQuery("get_proposal", GtvObjectMapper.toGtvDictionary(ProposalData(
                        id = proposalId,
                        type = ProposalType.force_configuration,
                        proposedBy = proposedBy,
                        timestamp = 1234567890000L,
                        description = "Force configuration test",
                        state = ProposalState.PENDING,
                        applyAt = null,
                        scheduledAt = null,
                        txRid = WrappedByteArray(32),
                        voterSetName = null
                )))
                .withDCQuery("get_proposal_voting_results", GtvObjectMapper.toGtvDictionary(ProposalVotingResults(
                        positiveVotes = 1,
                        negativeVotes = 0,
                        maxVotes = 3,
                        threshold = 2,
                        votingResult = VotingResult.pending,
                        voterSetName = null
                )))
                .withDCQuery("get_proposal_voter_info", gtv(listOf()))
                .withDCQuery(GET_FORCED_CONFIGURATION_PROPOSAL_V64, GtvObjectMapper.toGtvDictionary(
                        ForcedConfigurationProposalData(
                                blockchainRid = blockchainRid,
                                currentHeight = 17,
                                currentConfiguration = GtvEncoder.encodeGtv(currentConfig).wrap(),
                                proposedHeight = 100,
                                proposedConfiguration = GtvEncoder.encodeGtv(proposedConfig).wrap(),
                                resumeChain = false
                        )
                ))
                .addDcGetProviderData()
                .testCommand(
                        CommandGetProposal(),
                        "--id", proposalId.id.toString()
                ) { result, _ ->
                    assertCommandSuccessContains(result, "Force for blockchain ${blockchainRid.toHex()} at height: 100")
                    assertCommandSuccessContains(result, "Resume-chain: false")
                    assertCommandSuccessContains(result, "Path: foo")
                    assertCommandSuccessContains(result, "Value changed from 17 to 23")
                }
    }

    @Test
    fun `force configuration proposal with old API version - shows height, resume-chain and diff`(@TempDir dir: Path) {
        val currentConfig = gtv(mapOf("setting" to gtv("alpha"), "count" to gtv(5)))
        val proposedConfig = gtv(mapOf("setting" to gtv("beta"), "count" to gtv(10)))
        
        ManagedRestTestApi(dir, dcVersion = 63)
                .withDCQuery("get_proposal", GtvObjectMapper.toGtvDictionary(ProposalData(
                        id = proposalId,
                        type = ProposalType.force_configuration,
                        proposedBy = proposedBy,
                        timestamp = 1234567890000L,
                        description = "Force configuration test old API",
                        state = ProposalState.PENDING,
                        applyAt = null,
                        scheduledAt = null,
                        txRid = WrappedByteArray(32),
                        voterSetName = null
                )))
                .withDCQuery("get_proposal_voting_results", GtvObjectMapper.toGtvDictionary(ProposalVotingResults(
                        positiveVotes = 2,
                        negativeVotes = 0,
                        maxVotes = 3,
                        threshold = 2,
                        votingResult = VotingResult.pending,
                        voterSetName = null
                )))
                .withDCQuery("get_proposal_voter_info", gtv(listOf()))
                .withDCQuery(GET_PROPOSED_FORCED_CONFIGURATION, GtvObjectMapper.toGtvDictionary(
                        GetProposedForcedConfigurationResult(
                                currentConf = BlockchainConfigurationData(
                                        height = 50,
                                        blockchain = RowId(1),
                                        data = GtvEncoder.encodeGtv(currentConfig).wrap()
                                ),
                                forcedConf = ProposedForcedConfigurationData(
                                        proposal = proposalId,
                                        blockchain = RowId(1),
                                        height = 200,
                                        configData = GtvEncoder.encodeGtv(proposedConfig).wrap(),
                                        resumeChain = true
                                ),
                                resumeChain = true
                        )
                ))
                .addDcGetProviderData()
                .testCommand(
                        CommandGetProposal(),
                        "--id", proposalId.id.toString()
                ) { result, _ ->
                    assertCommandSuccessContains(result, "Force at height: 200")
                    assertCommandSuccessContains(result, "Resume-chain: true")
                    assertCommandSuccessContains(result, "Path: setting")
                    assertCommandSuccessContains(result, "0- alpha")
                    assertCommandSuccessContains(result, "0+ beta")
                    assertCommandSuccessContains(result, "Path: count")
                    assertCommandSuccessContains(result, "Value changed from 5 to 10")
                }
    }

    @Test
    fun `force configuration proposal with old API version - resume chain false`(@TempDir dir: Path) {
        val currentConfig = gtv(mapOf("enabled" to gtv(true)))
        val proposedConfig = gtv(mapOf("enabled" to gtv(false)))
        
        ManagedRestTestApi(dir, dcVersion = 63)
                .withDCQuery("get_proposal", GtvObjectMapper.toGtvDictionary(ProposalData(
                        id = proposalId,
                        type = ProposalType.force_configuration,
                        proposedBy = proposedBy,
                        timestamp = 1234567890000L,
                        description = "Force configuration test old API",
                        state = ProposalState.PENDING,
                        applyAt = null,
                        scheduledAt = null,
                        txRid = WrappedByteArray(32),
                        voterSetName = null
                )))
                .withDCQuery("get_proposal_voting_results", GtvObjectMapper.toGtvDictionary(ProposalVotingResults(
                        positiveVotes = 1,
                        negativeVotes = 1,
                        maxVotes = 3,
                        threshold = 2,
                        votingResult = VotingResult.pending,
                        voterSetName = null
                )))
                .withDCQuery("get_proposal_voter_info", gtv(listOf()))
                .withDCQuery(GET_PROPOSED_FORCED_CONFIGURATION, GtvObjectMapper.toGtvDictionary(
                        GetProposedForcedConfigurationResult(
                                currentConf = BlockchainConfigurationData(
                                        height = 30,
                                        blockchain = RowId(1),
                                        data = GtvEncoder.encodeGtv(currentConfig).wrap()
                                ),
                                forcedConf = ProposedForcedConfigurationData(
                                        proposal = proposalId,
                                        blockchain = RowId(1),
                                        height = 150,
                                        configData = GtvEncoder.encodeGtv(proposedConfig).wrap(),
                                        resumeChain = false
                                ),
                                resumeChain = false
                        )
                ))
                .addDcGetProviderData()
                .testCommand(
                        CommandGetProposal(),
                        "--id", proposalId.id.toString()
                ) { result, _ ->
                    assertCommandSuccessContains(result, "Force at height: 150")
                    assertCommandSuccessContains(result, "Resume-chain: false")
                    assertCommandSuccessContains(result, "Path: enabled")
                }
    }

}

