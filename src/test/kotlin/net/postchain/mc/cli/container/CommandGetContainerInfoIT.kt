package net.postchain.mc.cli.container

import com.google.gson.Gson
import com.google.gson.JsonElement
import net.postchain.anchoring.anchoring_chain_cluster.ContainerResourceUsageStatistics
import net.postchain.anchoring.common_helpers.resource_usage_statistics.ResourceType
import net.postchain.common.hexStringToByteArray
import net.postchain.common.wrap
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.base.CLUSTER_ANCHORING_CHAIN_RESOURCE_USAGE_VERSION
import net.postchain.mc.cli.blockchain.buildCmGetClusterInfoResponse
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandFailureContains
import net.postchain.mc.cli.test_helpers.assertCommandOutputContains
import net.postchain.mc.cli.test_helpers.assertCommandOutputDoesNotContain
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import net.postchain.mc.cli.test_helpers.buildGetContainerDataResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.math.BigDecimal
import java.nio.file.Path
import java.time.Instant
import java.util.Date

class CommandGetContainerInfoIT {

    private val expireTimeMillis = 1767225600000L // 2026-01-01T00:00:00Z
    private val expireTime = Date.from(Instant.ofEpochMilli(expireTimeMillis)).toString()

    @Test
    fun `container info`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_container_data", buildGetContainerDataResponse())
                .withDCQuery("nm_get_container_limits", buildNmGetContainerLimits())
                .withDCQuery("get_container_blockchain", buildGetContainerBlockchain())
                .withECQuery("get_lease_by_container_name", buildGetLeaseByContainerName(expireTimeMillis))
                .testCommand(CommandGetContainerInfo(),
                        "--name", "container1",
                ) { result, _ ->
                    Gson().fromJson(result.stdout, JsonElement::class.java)

                    assertCommandOutputContains(result.output, """
                        {
                        "container_info": {
                          "Name": "container01",
                          "Cluster": "cluster01",
                          "Deployer": "deployer01",
                          "Proposed_by": "03F7AB0AD49CC99773832549222E140603EF85B0904B78554BE5B87236712DF37E / proposed-by-name01",
                          "System": "false",
                          "State": "RUNNING",
                          "Lease_expires": "$expireTime"
                        }
                        ,"resource_limits": [
                          {
                            "Resource_type": "container_units",
                            "Value": "1"
                          },
                          {
                            "Resource_type": "cpu",
                            "Value": "50 %"
                          },
                          {
                            "Resource_type": "io_read",
                            "Value": "25 MiB/s"
                          },
                          {
                            "Resource_type": "io_write",
                            "Value": "20 MiB/s"
                          },
                          {
                            "Resource_type": "max_blockchains",
                            "Value": "10"
                          },
                          {
                            "Resource_type": "ram",
                            "Value": "2048 MiB"
                          },
                          {
                            "Resource_type": "storage",
                            "Value": "16384 MiB"
                          }
                        ]
                        ,"blockchains": [
                        {
                            "Name": "dapp01",
                            "Rid": "2121212121212121212121212121212121212121212121212121212121212121",
                            "System": "false",
                            "State": "RUNNING"
                        }
                        ]
                        }
                    """.trimIndent())
                }
    }

    @Test
    fun `container info - expired lease`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_container_data", buildGetContainerDataResponse())
                .withDCQuery("nm_get_container_limits", buildNmGetContainerLimits())
                .withDCQuery("get_container_blockchain", buildGetContainerBlockchain())
                .withECQuery("get_lease_by_container_name", buildGetLeaseByContainerName(expireTimeMillis, expired = true))
                .testCommand(CommandGetContainerInfo(),
                        "--name", "container1",
                ) { result, _ ->
                    assertCommandSuccessContains(result, """"Lease_expires": "$expireTime (expired)"""")
                }
    }

    @Test
    fun `container info - no economy chain`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_container_data", buildGetContainerDataResponse())
                .withDCQuery("nm_get_container_limits", buildNmGetContainerLimits())
                .withDCQuery("get_container_blockchain", buildGetContainerBlockchain())
                .withDCQuery("get_economy_chain_rid", GtvNull)
                .testCommand(CommandGetContainerInfo(),
                        "--name", "container1",
                ) { result, _ ->
                    assertCommandSuccessContains(result, """"State": "RUNNING"""")
                    assertCommandOutputDoesNotContain(result.output, "Lease_expires")
                }
    }

    @Test
    fun `container info - resource usage`(@TempDir dir: Path) {
        val resourceUsage = gtv(
                listOf(
                        ContainerResourceUsageStatistics("000203".hexStringToByteArray().wrap(),
                                1750930864466L, ResourceType.space_usage_mib, BigDecimal("133")),
                        ContainerResourceUsageStatistics("040506".hexStringToByteArray().wrap(),
                                1750930865566L, ResourceType.space_usage_percentage, BigDecimal("17")),
                        ContainerResourceUsageStatistics("070809".hexStringToByteArray().wrap(),
                                1750930866666L, ResourceType.free_space_left_mib, BigDecimal("1500")),
                ).map(GtvObjectMapper::toGtvDictionary)
        )
        ManagedRestTestApi(dir)
                .withDCQuery("get_container_data", buildGetContainerDataResponse())
                .withDCQuery("nm_get_container_limits", buildNmGetContainerLimits())
                .withDCQuery("get_container_blockchain", buildGetContainerBlockchain())
                .withECQuery("get_lease_by_container_name", buildGetLeaseByContainerName(expireTimeMillis))
                .afterServerBeforeTest {
                    it.withDCQuery("cm_get_cluster_info", buildCmGetClusterInfoResponse(it.apiUrl, it.cacBcRid))
                }
                .withCACQuery("api_version", gtv(CLUSTER_ANCHORING_CHAIN_RESOURCE_USAGE_VERSION))
                .withCACQuery("get_latest_resource_usage_statistics", resourceUsage)
                .testCommand(CommandGetContainerInfo(),
                        "--name", "container1",
                        "--resource-usage"
                ) { result, _ ->
                    Gson().fromJson(result.stdout, JsonElement::class.java)

                    // Multiple asserts to exclude timestamp formatted by locale
                    assertCommandSuccessContains(result, """
                        "resource_usage": [
                    """.trimIndent())
                    assertCommandSuccessContains(result, """
                            {
                                "Node": "070809",
                                "Resource_type": "free_space_left_mib",
                                "Value": "1500",
                    """.trimIndent())
                    assertCommandSuccessContains(result, """
                            {
                                "Node": "000203",
                                "Resource_type": "space_usage_mib",
                                "Value": "133",
                    """.trimIndent())
                    assertCommandSuccessContains(result, """
                            {
                                "Node": "040506",
                                "Resource_type": "space_usage_percentage",
                                "Value": "17",
                    """.trimIndent())
                    assertCommandSuccessContains(result, """
                        }
                    """.trimIndent())
                }
    }

    @Test
    fun `container info - resource usage - old cac`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_container_data", buildGetContainerDataResponse())
                .withDCQuery("nm_get_container_limits", buildNmGetContainerLimits())
                .withDCQuery("get_container_blockchain", buildGetContainerBlockchain())
                .withECQuery("get_lease_by_container_name", buildGetLeaseByContainerName(expireTimeMillis))
                .afterServerBeforeTest {
                    it.withDCQuery("cm_get_cluster_info", buildCmGetClusterInfoResponse(it.apiUrl, it.cacBcRid))
                }
                .withCACQuery("api_version", gtv(CLUSTER_ANCHORING_CHAIN_RESOURCE_USAGE_VERSION - 1))
                .testCommand(CommandGetContainerInfo(),
                        "--name", "container1",
                        "--resource-usage"
                ) { result, _ ->
                    assertCommandFailureContains(result, "The cluster is running an older version of the cluster anchoring chain, which does not support resource usage statistics")
                }
    }
}
