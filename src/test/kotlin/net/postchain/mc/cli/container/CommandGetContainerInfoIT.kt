package net.postchain.mc.cli.container

import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandContains
import net.postchain.mc.cli.test_helpers.buildGetContainerDataResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandGetContainerInfoIT {

    @Test
    fun `direct container - fail due to dc version`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_container_data", buildGetContainerDataResponse())
                .withDCQuery("nm_get_container_limits", buildNmGetContainerLimits())
                .withDCQuery("get_container_blockchain", buildGetContainerBlockchain())
                .testCommand(CommandGetContainerInfo(),
                        "--name", "container1",
                ) { result, _ ->
                    assertCommandContains(result.output, """
                        {
                          "Name": "container01",
                          "Cluster": "cluster01",
                          "Deployer": "deployer01",
                          "Proposed_by": "03F7AB0AD49CC99773832549222E140603EF85B0904B78554BE5B87236712DF37E / proposed-by-name01",
                          "System": "false",
                          "State": "RUNNING"
                        }
                        [
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
                        """.trimIndent())
                    assertCommandContains(result.output, """
                        {
                            "Name": "dapp01",
                            "Rid": "2121212121212121212121212121212121212121212121212121212121212121",
                            "System": "false",
                            "State": "RUNNING"
                        }
                    """.trimIndent())
                }
    }
}
