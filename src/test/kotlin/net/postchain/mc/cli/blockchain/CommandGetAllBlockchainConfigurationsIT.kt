package net.postchain.mc.cli.blockchain

import assertk.assertThat
import assertk.assertions.isEqualTo
import net.postchain.chain0.model.BlockchainState
import net.postchain.common.BlockchainRid
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.buildGetBlockchainInfoResponse
import net.postchain.mc.cli.test_helpers.buildNmFindNextConfigurationHeightResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandGetAllBlockchainConfigurationsIT {

    @Test
    fun `basic test - return heights`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .withDCQuery("nm_find_next_configuration_height", gtv(100L), gtv(150), GtvNull)
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        ) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo("""
                        Configurations at heights found:
                        0
                        100
                        150
                    """.trimIndent())
                }
    }

    @Test
    fun `parameters passed to query`(@TempDir dir: Path) {
        val configHeights = listOf(100, 150, 200)
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .withDCQuery("nm_find_next_configuration_height",buildNmFindNextConfigurationHeightResponse(configHeights))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.buildRepeat(5).toHex(),
                        "--from-height", "100",
                        "--to-height", "160"
                        ) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo("""
                        Configurations at heights found:
                        100
                        150
                    """.trimIndent())
                }
    }
}