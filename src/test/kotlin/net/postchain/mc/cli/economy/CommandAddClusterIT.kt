package net.postchain.mc.cli.economy

import assertk.assertThat
import assertk.assertions.contains
import net.postchain.economy.economy_chain.createClusterOperation
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandAddClusterIT {

    @Test
    fun `economy add cluster - with default values`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = 2)
                .testCommand(CommandAddCluster(),
                        "--name", "cluster1",
                        "--voter-set", "vs1",
                        "--governor", "vs2",
                        "--tag", "tag1",
                ) { result, api ->
                    assertThat(result.output).contains("Proposal for creating cluster cluster1 is created")
                    api.getEcModel().assertCalledOps {
                        it.createClusterOperation(
                                "cluster1",
                                "vs2",
                                "vs1",
                                1,
                                0,
                                "tag1"
                        )
                    }
                }
    }

    @Test
    fun `economy add cluster - with custom values`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = 2)
                .testCommand(CommandAddCluster(),
                        "--name", "cluster1",
                        "--voter-set", "vs1",
                        "--governor", "vs2",
                        "--tag", "tag1",
                        "--cluster-units", "2",
                        "--extra-storage", "100",
                ) { result, api ->
                    assertThat(result.output).contains("Proposal for creating cluster cluster1 is created")
                    api.getEcModel().assertCalledOps {
                        it.createClusterOperation(
                                "cluster1",
                                "vs2",
                                "vs1",
                                2,
                                100,
                                "tag1"
                        )
                    }
                }
    }
}