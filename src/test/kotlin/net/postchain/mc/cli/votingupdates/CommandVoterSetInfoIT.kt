package net.postchain.mc.cli.votingupdates

import com.google.gson.Gson
import com.google.gson.JsonElement
import net.postchain.chain0.common.queries.GetVoterSetInfoResult
import net.postchain.common.hexStringToByteArray
import net.postchain.common.wrap
import net.postchain.gtv.Gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.provider.buildGetAllProvidersResponse
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER02_PUBKEY
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER03_PUBKEY
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandOutputContains
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandVoterSetInfoIT {

    @Test
    fun `voter set info`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_voter_set_info", buildGetVoterSetInfoResponse(
                        listOf(DEFAULT_PROVIDER01_PUBKEY, DEFAULT_PROVIDER02_PUBKEY)))
                .withDCQuery("get_all_providers", buildGetAllProvidersResponse())
                .testCommand(CommandVoterSetInfo(), "--name", "SYSTEM_P") { result, _ ->
                    Gson().fromJson(result.stdout, JsonElement::class.java)

                    assertCommandOutputContains(result.output, """
                        {
                        "basic": {
                          "Voter_set": "SYSTEM_P",
                          "Governed_by": "SYSTEM_P",
                          "Threshold": "super majority (\u003e66.66%)"
                        }
                        ,"providers": [
                          {
                            "Name": "provider01",
                            "Pubkey": "$DEFAULT_PROVIDER01_PUBKEY"
                          },
                          {
                            "Name": "provider02",
                            "Pubkey": "$DEFAULT_PROVIDER02_PUBKEY"
                          }
                        ]
                        }
                    """.trimIndent())
                }
    }

    @Test
    fun `voter set info - member which is not a known provider has no name`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_voter_set_info", buildGetVoterSetInfoResponse(listOf(DEFAULT_PROVIDER03_PUBKEY)))
                .withDCQuery("get_all_providers", buildGetAllProvidersResponse())
                .testCommand(CommandVoterSetInfo(), "--name", "SYSTEM_P") { result, _ ->
                    Gson().fromJson(result.stdout, JsonElement::class.java)

                    assertCommandOutputContains(result.output, """
                        ,"providers": [
                          {
                            "Name": "",
                            "Pubkey": "$DEFAULT_PROVIDER03_PUBKEY"
                          }
                        ]
                    """.trimIndent())
                }
    }

    @Test
    fun `voter set info - no members`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_voter_set_info", buildGetVoterSetInfoResponse(listOf()))
                .withDCQuery("get_all_providers", buildGetAllProvidersResponse())
                .testCommand(CommandVoterSetInfo(), "--name", "SYSTEM_P") { result, _ ->
                    Gson().fromJson(result.stdout, JsonElement::class.java)

                    assertCommandOutputContains(result.output, """
                        ,"providers": []
                    """.trimIndent())
                }
    }

    private fun buildGetVoterSetInfoResponse(members: List<String>): Gtv = GtvObjectMapper.toGtvDictionary(
            GetVoterSetInfoResult(
                    name = "SYSTEM_P",
                    threshold = 0L,
                    governor = "SYSTEM_P",
                    members = members.map { it.hexStringToByteArray().wrap() }
            )
    )
}
