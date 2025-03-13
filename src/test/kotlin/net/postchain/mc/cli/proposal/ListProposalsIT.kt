package net.postchain.mc.cli.proposal

import com.chromia.build.tools.TestProcess
import com.chromia.build.tools.restapi.TestModel
import net.postchain.api.rest.controller.Model
import net.postchain.api.rest.controller.RestApi
import net.postchain.chain0.proposal.GetRelevantProposalsResult
import net.postchain.chain0.proposal.ProposalState
import net.postchain.chain0.proposal.ProposalType
import net.postchain.chain0.proposal.voting.GetProviderVotesResult
import net.postchain.common.BlockchainRid
import net.postchain.common.exception.UserMistake
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.common.types.RowId
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtx.GtxQuery
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class ListProposalsIT {
    companion object {
        val dcBcRid = BlockchainRid.buildRepeat(1)
        const val PUBKEY = "03F7AB0AD49CC99773832549222E140603EF85B0904B78554BE5B87236712DF37E"
        const val PROVIDER_PUBKEY = "027CABF61ABED97E6D6B51C7388B689D01C9F3F06D3DC5299791AB548FBC61BF89"
    }

    class D1TestModel(private val model: Model, private val apiUrl: String) : Model by model {
        constructor(blockchainRid: BlockchainRid, apiUrl: String) : this(TestModel(blockchainRid), apiUrl)

        override fun query(query: GtxQuery) = when (query.name) {
            "api_version" -> gtv(81)
            "cm_get_blockchain_api_urls" -> gtv(gtv(apiUrl))
            "get_relevant_proposals" -> {
                val myPubkey = query.args.asDict()["my_pubkey"]!!.asByteArray()
                if (myPubkey.contentEquals(PROVIDER_PUBKEY.hexStringToByteArray())) {
                    gtv(listOf(GtvObjectMapper.toGtvDictionary(GetRelevantProposalsResult(
                            rowid = RowId(1L),
                            proposalType = ProposalType.configuration,
                            state = ProposalState.APPROVED
                    ))))
                } else {
                    throw UserMistake("Invalid pubkey: ${myPubkey.toHex()}")
                }
            }
            "get_provider_votes" -> gtv(listOf(GtvObjectMapper.toGtvDictionary(GetProviderVotesResult(RowId(1L), true))))
            else -> throw IllegalArgumentException("Query not found: ${query.name}")
        }
    }

    @Test
    fun test(@TempDir dir: Path) {
        RestApi(0, "", gracefulShutdown = false).use {
            val apiUrl = "http://localhost:${it.server.port()}"
            val dcModel = D1TestModel(dcBcRid, apiUrl)
            it.attachModel(dcBcRid, dcModel)
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                    api.url = $apiUrl
                    provider.pubkey=$PROVIDER_PUBKEY
                    pubkey=$PUBKEY
                    privkey=DC36585B89DD64D2F3A107FDA37C1730BEF3B3B13D5845B87C46EE235D0E9827
                    """.trimIndent())
            }

            TestProcess.Builder("proposal", "list")
                    .awaitCompletion(true)
                    .setWorkingDir(dir.toFile())
                    .exitCode(0)
                    .wholeOutput("""
                    [
                      {
                        "Type": "configuration",
                        "Id": "1",
                        "State": "APPROVED",
                        "Your_vote": "Accept"
                      }
                    ]
                    """.trimIndent())
                    .start()
        }
    }
}
