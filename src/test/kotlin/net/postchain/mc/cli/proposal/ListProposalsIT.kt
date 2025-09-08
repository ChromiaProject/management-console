package net.postchain.mc.cli.proposal

import assertk.assertThat
import assertk.assertions.isEqualTo
import net.postchain.chain0.proposal.ProposalState
import net.postchain.chain0.proposal.ProposalType
import net.postchain.chain0.proposal.voting.GetProviderVotesResult
import net.postchain.common.types.RowId
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi.Companion.DEFAULT_PROVIDER_PUBKEY
import net.postchain.mc.cli.test_helpers.buildListProposalQueryResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class ListProposalsIT {

    @Test
    fun test(@TempDir dir: Path) {
        ManagedRestTestApi(dir, providerPubKey = DEFAULT_PROVIDER_PUBKEY)
                .withDCQuery("get_relevant_proposals",
                        buildListProposalQueryResponse(DEFAULT_PROVIDER_PUBKEY, listOf(
                                Triple(1L, ProposalType.configuration, ProposalState.APPROVED))))
                .withDCQuery("get_provider_votes", gtv(listOf(GtvObjectMapper.toGtvDictionary(GetProviderVotesResult(RowId(1L), true)))))
                .testCommand(CommandListProposals()) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo("""
                    [
                      {
                        "Type": "configuration",
                        "Id": "1",
                        "State": "APPROVED",
                        "Your_vote": "Accept"
                      }
                    ]
                    """.trimIndent())
                }
    }
}
