package net.postchain.mc.cli.cluster

import net.postchain.chain0.common.queries.ContainerUnitResourceLimits
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.addDcEmptyListQueries
import net.postchain.mc.cli.test_helpers.assertCommandSuccess
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandGetClusterInfoIT {

    @Test
    fun `cluster info - container units`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 88)
                .addGetClusterData()
                .addGetClusterProviders()
                .addGetClusterNodes()
                .addDcEmptyListQueries("get_cluster_replica_nodes", "get_cluster_containers", "get_cluster_subnode_images")
                .withDCQuery("get_cluster_container_unit_limits",
                        GtvObjectMapper.toGtvDictionary(ContainerUnitResourceLimits(60, 1500, 10, 5, 1638)))
                .testCommand(
                        CommandGetClusterInfo(),
                        "--name", "cluster1",
                ) { result, _ ->
                    assertCommandSuccess(result, """
                          {
                            "Resource": "CPU",
                            "Limit": "60"
                          },
                          {
                            "Resource": "RAM",
                            "Limit": "1500"
                          },
                          {
                            "Resource": "I/O read",
                            "Limit": "10"
                          },
                          {
                            "Resource": "I/O write",
                            "Limit": "5"
                          },
                          {
                            "Resource": "Storage",
                            "Limit": "1638"
                          }
                    """.trimIndent())
                }
    }
}
