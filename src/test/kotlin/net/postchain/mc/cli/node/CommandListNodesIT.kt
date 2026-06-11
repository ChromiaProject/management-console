package net.postchain.mc.cli.node

import assertk.assertThat
import assertk.assertions.doesNotContain
import net.postchain.chain0.common.queries.GetAllNodesResult
import net.postchain.chain0.common.queries.GetClusterNodesResult
import net.postchain.chain0.common.queries.NodeInfo
import net.postchain.chain0.model.Provider
import net.postchain.chain0.model.ProviderTier
import net.postchain.common.hexStringToByteArray
import net.postchain.common.wrap
import net.postchain.gtv.GtvArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER02_PUBKEY
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertLineValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandListNodesIT {

    @Test
    fun `list nodes - no filter lists all nodes`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 47)
                .withDCQuery("get_all_nodes", buildGetAllNodesResponse())
                .testCommand(CommandListNodes()) { result, _ ->
                    assertLineValue(result.stdout, "Host", "node01")
                    assertLineValue(result.stdout, "Host", "node02")
                }
    }

    @Test
    fun `list nodes - filter by cluster`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 47)
                .withDCQuery("get_all_nodes", buildGetAllNodesResponse())
                .withDCQuery("get_cluster_nodes", buildGetClusterNodesResponse(DEFAULT_PROVIDER01_PUBKEY, "node01"))
                .testCommand(CommandListNodes(), "--cluster", "cluster01") { result, _ ->
                    assertLineValue(result.stdout, "Host", "node01")
                    assertThat(result.stdout).doesNotContain("node02")
                }
    }

    @Test
    fun `list nodes - filter by cluster without nodes lists nothing`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 47)
                .withDCQuery("get_all_nodes", buildGetAllNodesResponse())
                .withDCQuery("get_cluster_nodes", gtv(listOf()))
                .testCommand(CommandListNodes(), "--cluster", "cluster01") { result, _ ->
                    assertThat(result.stdout).doesNotContain("node01")
                    assertThat(result.stdout).doesNotContain("node02")
                }
    }

    private fun buildGetAllNodesResponse(): GtvArray {
        return gtv(listOf(
                buildNode(DEFAULT_PROVIDER01_PUBKEY, "node01", "provider01"),
                buildNode(DEFAULT_PROVIDER02_PUBKEY, "node02", "provider02")
        ).map(GtvObjectMapper::toGtvDictionary))
    }

    private fun buildNode(pubkey: String, host: String, providerName: String): GetAllNodesResult {
        return GetAllNodesResult(
                NodeInfo(
                        pubkey.hexStringToByteArray().wrap(),
                        host,
                        9870,
                        "http://$host:7740",
                        null
                ),
                true,
                0,
                Provider(
                        pubkey.hexStringToByteArray().wrap(),
                        providerName,
                        "http://$providerName:7740",
                        true,
                        ProviderTier.NODE_PROVIDER,
                        true
                )
        )
    }

    private fun buildGetClusterNodesResponse(pubkey: String, host: String): GtvArray {
        return gtv(listOf(
                GetClusterNodesResult(
                        pubkey.hexStringToByteArray().wrap(),
                        host,
                        9870,
                        "http://$host:7740",
                        true
                )
        ).map(GtvObjectMapper::toGtvDictionary))
    }
}
