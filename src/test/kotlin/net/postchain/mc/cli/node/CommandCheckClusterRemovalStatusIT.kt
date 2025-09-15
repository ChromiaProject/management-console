package net.postchain.mc.cli.node

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.google.gson.Gson
import com.google.gson.JsonElement
import net.postchain.common.BlockchainRid
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.blockchain.buildCmGetClusterInfoResponse
import net.postchain.mc.cli.blockchain.buildGetLastAnchoredBlockResponse
import net.postchain.mc.cli.test_helpers.DEFAULT_BRID_CLUSTER_ANCHORING_CHAIN
import net.postchain.mc.cli.test_helpers.DEFAULT_BRID_DIRECTORY_CHAIN
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CommandCheckClusterRemovalStatusIT {
    private val nodeKey = ByteArray(33)
    private val cluster = "system"

    @Test
    fun `Report successful removal when no longer signer`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 99)
                .withDCQuery(
                        "get_node_signer_cluster_blockchains",
                        gtv(listOf())
                )
                .testCommand(CommandCheckClusterRemovalStatus(),
                        "--pubkey", nodeKey.toHex(),
                        "--cluster", cluster
                ) { result, _ ->
                    val outputJson = Gson().fromJson(result.stdout, JsonElement::class.java)
                    assertThat(outputJson.asJsonArray).isEmpty()
                }
    }

    @Test
    fun `Reports correct anchored heights for regular chains and CACs`(@TempDir dir: Path) {
        // We don't have blockAtHeight support in test framework so let's skip it
        val lastAnchorTimeDc = System.currentTimeMillis() - 60_000L
        val lastAnchorTimeCac = System.currentTimeMillis() - 120_000L
        ManagedRestTestApi(dir, dcVersion = 99)
                .withDCQuery(
                        "cm_get_cluster_info",
                        buildCmGetClusterInfoResponse(clusterAnchoringBrid = DEFAULT_BRID_CLUSTER_ANCHORING_CHAIN)
                )
                .withDCQuery(
                        "get_node_signer_cluster_blockchains",
                        gtv(listOf(gtv(DEFAULT_BRID_DIRECTORY_CHAIN), gtv(DEFAULT_BRID_CLUSTER_ANCHORING_CHAIN)))
                )
                .withCACQuery("get_last_anchored_block", buildGetLastAnchoredBlockResponse(100, lastAnchorTimeDc))
                .withSACQuery("get_last_anchored_block", buildGetLastAnchoredBlockResponse(101, lastAnchorTimeCac))
                .testCommand(CommandCheckClusterRemovalStatus(),
                        "--pubkey", nodeKey.toHex(),
                        "--cluster", cluster
                ) { result, _ ->
                    val outputJson = Gson().fromJson(result.stdout, JsonElement::class.java)
                    val expectedDateTimes = mapOf(
                            DEFAULT_BRID_DIRECTORY_CHAIN to formatTimestamp(lastAnchorTimeDc),
                            DEFAULT_BRID_CLUSTER_ANCHORING_CHAIN to formatTimestamp(lastAnchorTimeCac),
                    )

                    outputJson.asJsonArray.forEach {
                        assertThat(it.asJsonObject["Last_anchored_block_time"].asString).isEqualTo(expectedDateTimes[BlockchainRid(it.asJsonObject["Blockchain_RID"].asString.hexStringToByteArray())])
                    }
                }
    }

    private fun formatTimestamp(timestamp: Long) = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))
}