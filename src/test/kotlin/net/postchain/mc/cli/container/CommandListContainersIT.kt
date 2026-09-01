package net.postchain.mc.cli.container

import net.postchain.chain0.common.queries.GetContainersResult
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtx.GtxQuery
import net.postchain.mc.cli.blockchain.buildGetBlockchainInfoListResponse
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandOutputDoesNotContain
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CommandListContainersIT {

    private val expireTimeMillis = 1767225600000L // 2026-01-01T00:00:00Z
    private val expireTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(expireTimeMillis))

    @Test
    fun `list containers`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_containers", buildGetContainersResponse())
                .withDCQuery("get_blockchain_info_list", buildGetBlockchainInfoListResponse())
                .withECQuery("get_lease_by_container_name", buildGetLeaseByContainerNameByQuery())
                .testCommand(CommandListContainers()) { result, _ ->
                    assertCommandSuccessContains(result, """
                        {
                          "Name": "container01",
                          "Cluster": "cluster01",
                          "Deployer_voter_set": "deployer01",
                          "Lease_expires": "$expireTime",
                          "Blockchains": "bc01"
                        }
                    """.trimIndent())
                    // container02 has no lease
                    assertCommandSuccessContains(result, """
                        {
                          "Name": "container02",
                          "Cluster": "cluster01",
                          "Deployer_voter_set": "deployer02",
                          "Lease_expires": "",
                          "Blockchains": "bc02"
                        }
                    """.trimIndent())
                }
    }

    @Test
    fun `list containers - no economy chain`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_containers", buildGetContainersResponse())
                .withDCQuery("get_blockchain_info_list", buildGetBlockchainInfoListResponse())
                .withDCQuery("get_economy_chain_rid", GtvNull)
                .testCommand(CommandListContainers()) { result, _ ->
                    assertCommandSuccessContains(result, """"Name": "container01"""")
                    assertCommandOutputDoesNotContain(result.output, "Lease_expires")
                }
    }

    private fun buildGetContainersResponse(): Gtv = gtv(listOf(
            GetContainersResult("cluster01", "container01", "deployer01"),
            GetContainersResult("cluster01", "container02", "deployer02"),
    ).map(GtvObjectMapper::toGtvDictionary))

    private fun buildGetLeaseByContainerNameByQuery(): (query: GtxQuery) -> Gtv = { query ->
        if (query.args["container_name"]?.asString() == "container01")
            buildGetLeaseByContainerName(expireTimeMillis)
        else
            GtvNull
    }
}
