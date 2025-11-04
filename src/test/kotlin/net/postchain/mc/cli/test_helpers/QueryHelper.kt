package net.postchain.mc.cli.test_helpers

import net.postchain.chain0.common.queries.ContainerData
import net.postchain.chain0.model.ContainerState
import net.postchain.chain0.proposal.GetRelevantProposalsResult
import net.postchain.chain0.proposal.ProposalState
import net.postchain.chain0.proposal.ProposalType
import net.postchain.common.exception.UserMistake
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.common.types.RowId
import net.postchain.common.wrap
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtx.GtxQuery
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi.Companion.DEFAULT_PROVIDER_PUBKEY

// Helper function to build a proposal list response
fun buildListProposalQueryResponse(pubkey: String, proposals: List<Triple<Long, ProposalType, ProposalState>>): (query: GtxQuery) -> Gtv {
    return { query ->
        val myPubkey = query.args.asDict()["my_pubkey"]!!.asByteArray()
        if (myPubkey.contentEquals(pubkey.hexStringToByteArray())) {
            gtv(
                    proposals.map {
                        GtvObjectMapper.toGtvDictionary(GetRelevantProposalsResult(
                                rowid = RowId(it.first),
                                proposalType = it.second,
                                state = it.third
                        ))
                    }
            )
        } else {
            throw UserMistake("Invalid pubkey: ${myPubkey.toHex()}")
        }
    }
}

fun buildGetContainerDataResponse(): Gtv {
    return GtvObjectMapper.toGtvDictionary(ContainerData(
            "container01",
            "cluster01",
            "deployer01",
            DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray().wrap(),
            "proposed-by-name01",
            false,
            ContainerState.RUNNING,
            null,
            listOf()
    ))
}

fun ManagedRestTestApi.addDcEmptyListQueries(vararg queries: String): ManagedRestTestApi {
    queries.forEach {
        withDCQuery(it, gtv(listOf()))
    }
    return this
}

