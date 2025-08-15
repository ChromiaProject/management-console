package net.postchain.mc.cli.cluster

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.chromia.build.tools.restapi.TestModel
import net.postchain.mc.cli.blockchain.buildCmGetClusterBlockchainsResponse
import net.postchain.mc.cli.blockchain.buildCmGetClusterInfoResponse
import net.postchain.mc.cli.blockchain.buildCmGetSystemAnchoringChainResponse
import net.postchain.mc.cli.blockchain.buildGetLastAnchoredBlockResponse
import net.postchain.mc.cli.blockchain.buildGetNodeDataResponse
import net.postchain.mc.cli.test_helpers.DEFAULT_DAPP_RID
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.RestTestModel
import net.postchain.mc.cli.test_helpers.assertLineValue
import net.postchain.mc.cli.test_helpers.testPmcCommand
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandClusterVerifyIT {
    @Test
    fun basics(@TempDir dir: Path) {
        val managedTest = ManagedRestTestApi(dir, models = mutableMapOf(DEFAULT_DAPP_RID to RestTestModel(100, TestModel(DEFAULT_DAPP_RID))))
                .withDCQuery("cm_get_cluster_blockchains", buildCmGetClusterBlockchainsResponse())
                .withDCQuery("cm_get_system_anchoring_chain", buildCmGetSystemAnchoringChainResponse())
                .withCACQuery("get_last_anchored_block", buildGetLastAnchoredBlockResponse(112230))
                .afterServerBeforeTest {
                    it.withDCQuery("get_node_data", buildGetNodeDataResponse(it.apiUrl))
                }
        managedTest.test {
            managedTest.withDCQuery("cm_get_cluster_info", buildCmGetClusterInfoResponse(managedTest.apiUrl, managedTest.cacBcRid))
            managedTest.withModel(DEFAULT_DAPP_RID) {
                it.height = 112233
            }
            val result = testPmcCommand(managedTest.dir, CommandClusterVerify(),
                    "--name", "cluster01"
            )
            assertThat(result.statusCode).isEqualTo(0)
            assertLineValue(result.stdout, "Blockchain_RID", DEFAULT_DAPP_RID.toHex())
            assertLineValue(result.stdout, "Anchored", "112230")
            assertLineValue(result.stdout, ManagedRestTestApi.DEFAULT_PROVIDER_PUBKEY, "112233")
        }
    }
}