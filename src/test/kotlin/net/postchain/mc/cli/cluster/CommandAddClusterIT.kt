package net.postchain.mc.cli.cluster

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.postchain.chain0.direct_cluster.createClusterFromWithClusterDataOperation
import net.postchain.chain0.direct_cluster.createClusterWithClusterDataOperation
import net.postchain.chain0.direct_cluster.createClusterWithUnitsOperation
import net.postchain.chain0.model.ClusterCreationData
import net.postchain.common.hexStringToByteArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi.Companion.DEFAULT_PROVIDER_PUBKEY
import net.postchain.mc.compatibility.ApiCompatV28
import net.postchain.mc.compatibility.ApiCompatV28.createClusterWithClusterQuotaDataOperationV28
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandAddClusterIT {

    @Test
    fun `add cluster - fail when network uses EC`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 50)
                .withDCQuery("has_direct_cluster", gtv(false))
                .testCommand(CommandAddCluster(),
                        "--name", "cluster1",
                        "--pubkeys", DEFAULT_PROVIDER_PUBKEY,
                        "--governor", "vs1",
                ) { result, _ ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.output).contains("Network is configured to work with EC")
                }
    }

    @Test
    fun `add cluster - with pubkeys - api v34+`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 34)
                .withDCQuery("has_direct_cluster", gtv(true))
                .testCommand(CommandAddCluster(),
                        "--name", "cluster1",
                        "--pubkeys", DEFAULT_PROVIDER_PUBKEY,
                        "--governor", "vs1",
                        "--cluster-units", "2",
                        "--extra-storage", "100",
                ) { result, api ->
                    assertThat(result.output).contains("Cluster cluster1 added")
                    api.getDcModel().assertCalledOps {
                        it.createClusterWithClusterDataOperation(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "cluster1",
                                "vs1",
                                listOf(DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray()),
                                ClusterCreationData(2, 100)
                        )
                    }
                }
    }

    @Test
    fun `add cluster - with voter set - api v34+`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 34)
                .withDCQuery("has_direct_cluster", gtv(true))
                .testCommand(CommandAddCluster(),
                        "--name", "cluster1",
                        "--voter-set", "vs2",
                        "--governor", "vs1",
                        "--cluster-units", "2",
                        "--extra-storage", "100",
                ) { result, api ->
                    assertThat(result.output).contains("Cluster cluster1 added")
                    api.getDcModel().assertCalledOps {
                        it.createClusterFromWithClusterDataOperation(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "cluster1",
                                "vs1",
                                "vs2",
                                ClusterCreationData(2, 100)
                        )
                    }
                }
    }

    @Test
    fun `add cluster - with pubkeys - api v24-33`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 24)
                .withDCQuery("has_direct_cluster", gtv(true))
                .testCommand(CommandAddCluster(),
                        "--name", "cluster1",
                        "--pubkeys", DEFAULT_PROVIDER_PUBKEY,
                        "--governor", "vs1",
                        "--cluster-units", "2",
                        "--extra-storage", "100",
                ) { result, api ->
                    assertThat(result.output).contains("Cluster cluster1 added")
                    api.getDcModel().assertCalledOps {
                        it.createClusterWithClusterQuotaDataOperationV28(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "cluster1",
                                "vs1",
                                listOf(DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray()),
                                ApiCompatV28.ClusterQuotaDataV28(2, 100)
                        )
                    }
                }
    }

    @Test
    fun `add cluster - with pubkeys - api v3-23`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 3)
                .withDCQuery("has_direct_cluster", gtv(true))
                .testCommand(CommandAddCluster(),
                        "--name", "cluster1",
                        "--pubkeys", DEFAULT_PROVIDER_PUBKEY,
                        "--governor", "vs1",
                        "--cluster-units", "2",
                ) { result, api ->
                    assertThat(result.output).contains("Cluster cluster1 added")
                    api.getDcModel().assertCalledOps {
                        it.createClusterWithUnitsOperation(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "cluster1",
                                "vs1",
                                listOf(DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray()),
                                2
                        )
                    }
                }
    }
}