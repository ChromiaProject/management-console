package net.postchain.mc.cli.economy

import assertk.assertThat
import assertk.assertions.contains
import net.postchain.common.hexStringToByteArray
import net.postchain.economy.economy_chain.createClusterOperation
import net.postchain.mc.cli.base.ECONOMY_CHAIN_MAX_CLUSTER_NODES_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.compatibility.ApiCompatECV56.createClusterOperationV56
import net.postchain.mc.compatibility.ApiCompatECV62.createClusterOperationV62
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandAddClusterIT {

    @Test
    fun `economy add cluster - with custom container units`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_MAX_CLUSTER_NODES_VERSION)
                .testCommand(
                        CommandAddCluster(),
                        "--name", "cluster1",
                        "--voter-set", "vs1",
                        "--governor", "vs2",
                        "--tag", "tag1",
                        "--cu-cpu", "25",
                        "--cu-ram", "1024",
                        "--cu-storage", "6000",
                        "--cu-io-read", "10",
                        "--cu-io-write", "5",
                        "--system-container-units", "6"
                ) { result, api ->
                    assertThat(result.output).contains("Proposal for creating cluster cluster1 is created")
                    api.getEcModel().assertCalledOps {
                        it.createClusterOperation(
                                api.pubKey.hexStringToByteArray(),
                                "cluster1",
                                "vs2",
                                "vs1",
                                1,
                                0,
                                "tag1",
                                25,
                                1024,
                                10,
                                5,
                                6000,
                                6,
                                Long.MAX_VALUE
                        )
                    }
                }
    }

    @Test
    fun `economy add cluster - with default values`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_MAX_CLUSTER_NODES_VERSION)
                .testCommand(
                        CommandAddCluster(),
                        "--name", "cluster1",
                        "--voter-set", "vs1",
                        "--governor", "vs2",
                        "--tag", "tag1",
                ) { result, api ->
                    assertThat(result.output).contains("Proposal for creating cluster cluster1 is created")
                    api.getEcModel().assertCalledOps {
                        it.createClusterOperation(
                                api.pubKey.hexStringToByteArray(),
                                "cluster1",
                                "vs2",
                                "vs1",
                                1,
                                0,
                                "tag1",
                                50,
                                2048,
                                25,
                                20,
                                16384,
                                4,
                                Long.MAX_VALUE
                        )
                    }
                }
    }

    @Test
    fun `economy add cluster - with custom values`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_MAX_CLUSTER_NODES_VERSION)
                .testCommand(
                        CommandAddCluster(),
                        "--name", "cluster1",
                        "--voter-set", "vs1",
                        "--governor", "vs2",
                        "--tag", "tag1",
                        "--cluster-units", "2",
                        "--extra-storage", "100",
                        "--max-nodes", "10"
                ) { result, api ->
                    assertThat(result.output).contains("Proposal for creating cluster cluster1 is created")
                    api.getEcModel().assertCalledOps {
                        it.createClusterOperation(
                                api.pubKey.hexStringToByteArray(),
                                "cluster1",
                                "vs2",
                                "vs1",
                                2,
                                100,
                                "tag1",
                                50,
                                2048,
                                25,
                                20,
                                16384,
                                4,
                                10
                        )
                    }
                }
    }

    @Test
    fun `economy add cluster v62 - with custom values`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION)
                .testCommand(
                        CommandAddCluster(),
                        "--name", "cluster1",
                        "--voter-set", "vs1",
                        "--governor", "vs2",
                        "--tag", "tag1",
                        "--cluster-units", "2",
                        "--extra-storage", "100",
                        "--cu-cpu", "25",
                        "--cu-ram", "1024",
                        "--cu-storage", "6000",
                        "--cu-io-read", "10",
                        "--cu-io-write", "5",
                        "--system-container-units", "6"
                ) { result, api ->
                    assertThat(result.output).contains("Proposal for creating cluster cluster1 is created")
                    api.getEcModel().assertCalledOps {
                        it.createClusterOperationV62(
                                api.pubKey.hexStringToByteArray(),
                                "cluster1",
                                "vs2",
                                "vs1",
                                2,
                                100,
                                "tag1",
                                25,
                                1024,
                                10,
                                5,
                                6000,
                                6
                        )
                    }
                }
    }

    @Test
    fun `economy add cluster v56 - with default values`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = 2)
                .testCommand(
                        CommandAddCluster(),
                        "--name", "cluster1",
                        "--voter-set", "vs1",
                        "--governor", "vs2",
                        "--tag", "tag1",
                ) { result, api ->
                    assertThat(result.output).contains("Proposal for creating cluster cluster1 is created")
                    api.getEcModel().assertCalledOps {
                        it.createClusterOperationV56(
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
    fun `economy add cluster v56 - with custom values`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = 2)
                .testCommand(
                        CommandAddCluster(),
                        "--name", "cluster1",
                        "--voter-set", "vs1",
                        "--governor", "vs2",
                        "--tag", "tag1",
                        "--cluster-units", "2",
                        "--extra-storage", "100",
                ) { result, api ->
                    assertThat(result.output).contains("Proposal for creating cluster cluster1 is created")
                    api.getEcModel().assertCalledOps {
                        it.createClusterOperationV56(
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

    @Test
    fun `economy add cluster - with require provider identifier version`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_MAX_CLUSTER_NODES_VERSION)
                .testCommand(CommandAddCluster(),
                        "--name", "cluster1",
                        "--voter-set", "vs1",
                        "--governor", "vs2",
                        "--tag", "tag1",
                        "--cluster-units", "2",
                        "--extra-storage", "100",
                        "--max-nodes", "10"
                ) { result, api ->
                    assertThat(result.output).contains("Proposal for creating cluster cluster1 is created")
                    api.getEcModel().assertCalledOps {
                        it.createClusterOperation(
                                api.pubKey.hexStringToByteArray(),
                                "cluster1",
                                "vs2",
                                "vs1",
                                2,
                                100,
                                "tag1",
                                50,
                                2048,
                                25,
                                20,
                                16384,
                                4,
                                10
                        )
                    }
                }
    }
}