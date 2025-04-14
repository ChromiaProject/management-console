package net.postchain.mc.cli.economy

import net.postchain.common.types.RowId
import net.postchain.common.types.WrappedByteArray
import net.postchain.economy.common_proposal.CommonProposalData
import net.postchain.economy.common_proposal.CommonProposalState
import net.postchain.economy.common_proposal.CommonProposalType
import net.postchain.economy.common_proposal.CommonProposalVotingResults
import net.postchain.economy.common_proposal.CommonVotingResult
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi

fun ManagedRestTestApi.addEcGetCommonProposal(): ManagedRestTestApi {
    withECQuery("get_common_proposal", GtvObjectMapper.toGtvDictionary(CommonProposalData(
            RowId(123L),
            11122233334444,
            CommonProposalType.ec_cluster_create,
            WrappedByteArray(34),
            "description",
            CommonProposalState.PENDING,
            null,
            null
    )))
    return this
}

fun ManagedRestTestApi.addEcGetCommonProposalVotingResult(): ManagedRestTestApi {
    withECQuery("get_common_proposal_voting_results", GtvObjectMapper.toGtvDictionary(CommonProposalVotingResults(
            1, 0, 4, 3, CommonVotingResult.pending
    )))
    return this
}
