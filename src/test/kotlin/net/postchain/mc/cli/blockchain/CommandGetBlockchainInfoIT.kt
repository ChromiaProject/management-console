package net.postchain.mc.cli.blockchain

import com.google.gson.Gson
import com.google.gson.JsonElement
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.test_helpers.DEFAULT_BRID_DIRECTORY_CHAIN
import net.postchain.mc.cli.test_helpers.DEFAULT_NODE01_API
import net.postchain.mc.cli.test_helpers.DEFAULT_NODE01_HOST
import net.postchain.mc.cli.test_helpers.DEFAULT_NODE01_PORT
import net.postchain.mc.cli.test_helpers.DEFAULT_NODE01_PUBKEY
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandSuccess
import net.postchain.mc.cli.test_helpers.assertLineValue
import net.postchain.mc.cli.test_helpers.buildGetContainerDataResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandGetBlockchainInfoIT {
    @Test
    fun basics(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 63)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(configDelay = 100000, name = "bc01", rid = DEFAULT_BRID_DIRECTORY_CHAIN))
                .withDCQuery("get_container_data", buildGetContainerDataResponse())
                .withDCQuery("get_blockchain_replicas", buildGetBlockchainReplicasResponse())
                .withCACQuery("get_last_anchored_block", buildGetLastAnchoredBlockResponse(112230))
                .afterServerBeforeTest {
                    it.withDCQuery("cm_get_cluster_info", buildCmGetClusterInfoResponse(it.apiUrl, it.cacBcRid))
                    it.withDCQuery("get_node_data", buildGetNodeDataResponse(it.apiUrl))
                }
                .withDCModel {
                    it.height = 112233
                }
                .testCommand(
                        CommandGetBlockchainInfo(),
                        "-brid", DEFAULT_BRID_DIRECTORY_CHAIN.toHex(),
                ) { result, _ ->
                    assertCommandSuccess(result)
                    Gson().fromJson(result.stdout, JsonElement::class.java)

                    assertLineValue(result.stdout, "Name", "bc01")
                    assertLineValue(result.stdout, "RID", DEFAULT_BRID_DIRECTORY_CHAIN.toHex())
                    assertLineValue(result.stdout, "State", "RUNNING")
                    assertLineValue(result.stdout, "Container", "container01")
                    assertLineValue(result.stdout, "Cluster", "cluster01")
                    assertLineValue(result.stdout, "Is_system_chain", "false")
                    assertLineValue(result.stdout, "Configuration_delay", "100000")

                    // Anchored
                    assertLineValue(result.stdout, "Anchored_height", "112230")
                    assertLineValue(result.stdout, DEFAULT_PROVIDER01_PUBKEY, "112233")

                    // Nodes
                    assertLineValue(result.stdout, DEFAULT_NODE01_PUBKEY.toHex(), "112233")
                }
    }

    @Test
    fun moving(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 33)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(isMoving = true, name = "bc01", rid = DEFAULT_BRID_DIRECTORY_CHAIN))
                .withDCQuery("get_container_data", buildGetContainerDataResponse())
                .withDCQuery("get_blockchain_replicas", buildGetBlockchainReplicasResponse())
                .withDCQuery("get_moving_blockchain_info", buildGetMovingBlockchainInfoResponse())
                .withCACQuery("get_last_anchored_block", buildGetLastAnchoredBlockResponse(112230))
                .afterServerBeforeTest {
                    it.withDCQuery("cm_get_cluster_info", buildCmGetClusterInfoResponse(it.apiUrl, it.cacBcRid))
                    it.withDCQuery("get_node_data", buildGetNodeDataResponse(it.apiUrl))
                }
                .withDCModel {
                    it.height = 112233
                }
                .testCommand(
                        CommandGetBlockchainInfo(),
                        "-brid", DEFAULT_BRID_DIRECTORY_CHAIN.toHex(),
                ) { result, _ ->
                    assertCommandSuccess(result)
                    Gson().fromJson(result.stdout, JsonElement::class.java)

                    assertLineValue(result.stdout, "Source_container", "container01")
                    assertLineValue(result.stdout, "Destination_container", "container02")
                    assertLineValue(result.stdout, "Source_cluster", "cluster01")
                    assertLineValue(result.stdout, "Destination_cluster", "cluster01")
                    assertLineValue(result.stdout, "Final_height", "15000")
                }
    }

    @Test
    fun `foreign importing`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 33)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(isForeignImporting = true, name = "bc01", rid = DEFAULT_BRID_DIRECTORY_CHAIN))
                .withDCQuery("get_container_data", buildGetContainerDataResponse())
                .withDCQuery("get_blockchain_replicas", buildGetBlockchainReplicasResponse())
                .withDCQuery("get_importing_foreign_blockchain_info", buildGetImportingForeignBlockchainInfoResponse())
                .withCACQuery("get_last_anchored_block", buildGetLastAnchoredBlockResponse(112230))
                .afterServerBeforeTest {
                    it.withDCQuery("cm_get_cluster_info", buildCmGetClusterInfoResponse(it.apiUrl, it.cacBcRid))
                    it.withDCQuery("get_node_data", buildGetNodeDataResponse(it.apiUrl))
                }
                .withDCModel {
                    it.height = 112233
                }
                .testCommand(
                        CommandGetBlockchainInfo(),
                        "-brid", DEFAULT_BRID_DIRECTORY_CHAIN.toHex(),
                ) { result, _ ->
                    assertCommandSuccess(result)
                    Gson().fromJson(result.stdout, JsonElement::class.java)

                    assertLineValue(result.stdout, "Node_pubkey", DEFAULT_NODE01_PUBKEY.toHex())
                    assertLineValue(result.stdout, "Node_host", DEFAULT_NODE01_HOST)
                    assertLineValue(result.stdout, "Node_port", "$DEFAULT_NODE01_PORT")
                    assertLineValue(result.stdout, "Node_api-url", DEFAULT_NODE01_API)
                    assertLineValue(result.stdout, "Foreign_management_chain_RID", "0000000000000000000000000000000000000000000000000000000000000000")
                    assertLineValue(result.stdout, "Final_height", "16000")
                }
    }

    @Test
    fun unarchiving(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 33)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(isUnarchiving = true, name = "bc01, rid = DEFAULT_BRID_DIRECTORY_CHAIN"))
                .withDCQuery("get_container_data", buildGetContainerDataResponse())
                .withDCQuery("get_blockchain_replicas", buildGetBlockchainReplicasResponse())
                .withDCQuery("get_unarchiving_blockchain_info", buildGetUnarchivingBlockchainInfo())
                .withCACQuery("get_last_anchored_block", buildGetLastAnchoredBlockResponse(112230))
                .afterServerBeforeTest {
                    it.withDCQuery("cm_get_cluster_info", buildCmGetClusterInfoResponse(it.apiUrl, it.cacBcRid))
                    it.withDCQuery("get_node_data", buildGetNodeDataResponse(it.apiUrl))
                }
                .withDCModel {
                    it.height = 112233
                }
                .testCommand(
                        CommandGetBlockchainInfo(),
                        "-brid", DEFAULT_BRID_DIRECTORY_CHAIN.toHex(),
                ) { result, _ ->
                    assertCommandSuccess(result)
                    Gson().fromJson(result.stdout, JsonElement::class.java)

                    assertLineValue(result.stdout, "Source_container", "container01")
                    assertLineValue(result.stdout, "Destination_container", "container02")
                    assertLineValue(result.stdout, "Final_height", "15000")
                }
    }

    @Test
    fun `system chain by name`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 63)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(configDelay = 100000, rid = DEFAULT_BRID_DIRECTORY_CHAIN, name = "directory_chain"))
                .withDCQuery("get_container_data", buildGetContainerDataResponse())
                .withDCQuery("get_blockchain_replicas", buildGetBlockchainReplicasResponse())
                .withCACQuery("get_last_anchored_block", buildGetLastAnchoredBlockResponse(112230))
                .afterServerBeforeTest {
                    it.withDCQuery("cm_get_cluster_info", buildCmGetClusterInfoResponse(it.apiUrl, it.cacBcRid))
                    it.withDCQuery("get_node_data", buildGetNodeDataResponse(it.apiUrl))
                }
                .withDCModel {
                    it.height = 112233
                }
                .testCommand(
                        CommandGetBlockchainInfo(),
                        "-chain", "directory_chain",
                ) { result, _ ->
                    assertCommandSuccess(result)
                    Gson().fromJson(result.stdout, JsonElement::class.java)

                    assertLineValue(result.stdout, "Name", "directory_chain")
                    assertLineValue(result.stdout, "RID", DEFAULT_BRID_DIRECTORY_CHAIN.toHex())
                }
    }

    @Test
    fun `user chain by name`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 107)
                .withDCQuery("get_blockchain_info_by_name", buildGetBlockchainInfoResponse())
                .withDCQuery("get_container_data", buildGetContainerDataResponse())
                .withDCQuery("get_blockchain_replicas", buildGetBlockchainReplicasResponse())
                .withCACQuery("get_last_anchored_block", buildGetLastAnchoredBlockResponse(112230))
                .afterServerBeforeTest {
                    it.withDCQuery("cm_get_cluster_info", buildCmGetClusterInfoResponse(it.apiUrl, it.cacBcRid))
                    it.withDCQuery("get_node_data", buildGetNodeDataResponse(it.apiUrl))
                }
                .withDCModel {
                    it.height = 112233
                }
                .testCommand(
                        CommandGetBlockchainInfo(),
                        "-chain", "bc01",
                ) { result, _ ->
                    assertCommandSuccess(result)
                    Gson().fromJson(result.stdout, JsonElement::class.java)

                    assertLineValue(result.stdout, "Name", "bc01")
                    assertLineValue(result.stdout, "RID", BlockchainRid.ZERO_RID.toHex())
                }
    }
}
