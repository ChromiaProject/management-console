package net.postchain.mc.cli.node

import assertk.assertThat
import assertk.assertions.doesNotContain
import net.postchain.chain0.common.queries.GetAllNodesResult
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
    fun `list nodes - filter by system provider`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 47)
                .withDCQuery("get_all_nodes", buildGetAllNodesResponse())
                .testCommand(CommandListNodes(), "--system") { result, _ ->
                    assertLineValue(result.stdout, "Host", "node01")
                    assertLineValue(result.stdout, "Provided_by", DEFAULT_PROVIDER01_PUBKEY)
                    assertThat(result.stdout).doesNotContain("node02")
                }
    }

    @Test
    fun `list nodes - filter by non-system provider`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 47)
                .withDCQuery("get_all_nodes", buildGetAllNodesResponse())
                .testCommand(CommandListNodes(), "--non-system") { result, _ ->
                    assertLineValue(result.stdout, "Host", "node02")
                    assertLineValue(result.stdout, "Provided_by", DEFAULT_PROVIDER02_PUBKEY)
                    assertThat(result.stdout).doesNotContain("node01")
                }
    }

    private fun buildGetAllNodesResponse(): GtvArray {
        return gtv(listOf(
                GetAllNodesResult(
                        NodeInfo(
                                DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray().wrap(),
                                "node01",
                                9870,
                                "http://node01:7740",
                                null
                        ),
                        true,
                        0,
                        Provider(
                                DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray().wrap(),
                                "provider01",
                                "http://provider01:7740",
                                true,
                                ProviderTier.NODE_PROVIDER,
                                true
                        )
                ),
                GetAllNodesResult(
                        NodeInfo(
                                DEFAULT_PROVIDER02_PUBKEY.hexStringToByteArray().wrap(),
                                "node02",
                                9870,
                                "http://node02:7740",
                                null
                        ),
                        true,
                        0,
                        Provider(
                                DEFAULT_PROVIDER02_PUBKEY.hexStringToByteArray().wrap(),
                                "provider02",
                                "http://provider02:7740",
                                true,
                                ProviderTier.NODE_PROVIDER,
                                false
                        )
                )
        ).map(GtvObjectMapper::toGtvDictionary))
    }
}
