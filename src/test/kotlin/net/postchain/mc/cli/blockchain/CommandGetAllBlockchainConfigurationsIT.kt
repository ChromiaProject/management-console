package net.postchain.mc.cli.blockchain

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.postchain.chain0.model.BlockchainState
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandGetAllBlockchainConfigurationsIT {

    @Test
    fun `returns all configuration heights when no range specified`(@TempDir dir: Path) {
        val configHeights = listOf(0, 100, 150)
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .withDCQuery("nm_find_next_configuration_height", buildNmFindNextConfigurationHeightResponse(configHeights))
                .testCommand(
                        CommandGetAllBlockchainConfigurations(),
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
    fun `from-height matching exact config returns configs from that height`(@TempDir dir: Path) {
        val configHeights = listOf(0, 100, 150, 200, 250)
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .withDCQuery("nm_find_next_configuration_height", buildNmFindNextConfigurationHeightResponse(configHeights))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        "--from-height", "150"
                ) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo("""
                        Configurations at heights found:
                        150
                        200
                        250
                    """.trimIndent())
                }
    }

    @Test
    fun `from-height between configs returns configs starting from next available height`(@TempDir dir: Path) {
        val configHeights = listOf(0, 100, 150, 200, 250)
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .withDCQuery("nm_find_next_configuration_height", buildNmFindNextConfigurationHeightResponse(configHeights))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        "--from-height", "120"
                ) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo("""
                        Configurations at heights found:
                        150
                        200
                        250
                    """.trimIndent())
                }
    }

    @Test
    fun `from-height beyond all configs returns empty list`(@TempDir dir: Path) {
        val configHeights = listOf(0, 100, 150, 200)
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .withDCQuery("nm_find_next_configuration_height", buildNmFindNextConfigurationHeightResponse(configHeights))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        "--from-height", "300"
                ) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo("No configurations found")
                }
    }

    @Test
    fun `to-height matching exact config includes that config`(@TempDir dir: Path) {
        val configHeights = listOf(0, 100, 150, 200, 250)
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .withDCQuery("nm_find_next_configuration_height", buildNmFindNextConfigurationHeightResponse(configHeights))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        "--to-height", "200"
                ) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo("""
                        Configurations at heights found:
                        0
                        100
                        150
                        200
                    """.trimIndent())
                }
    }

    @Test
    fun `to-height between configs excludes configs after that height`(@TempDir dir: Path) {
        val configHeights = listOf(0, 100, 150, 200, 250)
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .withDCQuery("nm_find_next_configuration_height", buildNmFindNextConfigurationHeightResponse(configHeights))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        "--to-height", "180"
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
    fun `to-height below all configs returns only config at height 0`(@TempDir dir: Path) {
        val configHeights = listOf(0, 100, 150, 200)
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .withDCQuery("nm_find_next_configuration_height", buildNmFindNextConfigurationHeightResponse(configHeights))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        "--to-height", "50"
                ) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo("""
                        Configurations at heights found:
                        0
                    """.trimIndent())
                }
    }

    @Test
    fun `from-height and to-height with exact matching boundaries returns configs in range`(@TempDir dir: Path) {
        val configHeights = listOf(0, 100, 150, 200, 250, 300)
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .withDCQuery("nm_find_next_configuration_height", buildNmFindNextConfigurationHeightResponse(configHeights))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        "--from-height", "150",
                        "--to-height", "250"
                ) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo("""
                        Configurations at heights found:
                        150
                        200
                        250
                    """.trimIndent())
                }
    }

    @Test
    fun `from-height and to-height with single config in range returns only that config`(@TempDir dir: Path) {
        val configHeights = listOf(0, 100, 150, 200, 250)
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .withDCQuery("nm_find_next_configuration_height", buildNmFindNextConfigurationHeightResponse(configHeights))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        "--from-height", "140",
                        "--to-height", "180"
                ) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo("""
                        Configurations at heights found:
                        150
                    """.trimIndent())
                }
    }

    @Test
    fun `from-height and to-height with no configs in range returns empty list`(@TempDir dir: Path) {
        val configHeights = listOf(0, 100, 150, 200, 250)
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .withDCQuery("nm_find_next_configuration_height", buildNmFindNextConfigurationHeightResponse(configHeights))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        "--from-height", "300",
                        "--to-height", "400"
                ) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo("No configurations found")
                }
    }

    @Test
    fun `from-height equals to-height with exact match returns single config`(@TempDir dir: Path) {
        val configHeights = listOf(0, 100, 150, 200)
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .withDCQuery("nm_find_next_configuration_height", buildNmFindNextConfigurationHeightResponse(configHeights))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        "--from-height", "150",
                        "--to-height", "150"
                ) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo("""
                        Configurations at heights found:
                        150
                    """.trimIndent())
                }
    }

    @Test
    fun `from-height equals to-height with no match returns empty list`(@TempDir dir: Path) {
        val configHeights = listOf(0, 100, 150, 200)
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .withDCQuery("nm_find_next_configuration_height", buildNmFindNextConfigurationHeightResponse(configHeights))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        "--from-height", "125",
                        "--to-height", "125"
                ) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo("No configurations found")
                }
    }

    @Test
    fun `from-height with negative value fails validation`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        "--from-height", "-1"
                ) { result, _ ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.stderr).contains("--from-height must be non-negative")
                }
    }

    @Test
    fun `to-height with negative value fails validation`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        "--to-height", "-5"
                ) { result, _ ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.stderr).contains("--to-height must be non-negative")
                }
    }

    @Test
    fun `to-height less than from-height fails validation`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.RUNNING))
                .testCommand(CommandGetAllBlockchainConfigurations(),
                        "--blockchain-rid", BlockchainRid.ZERO_RID.toHex(),
                        "--from-height", "200",
                        "--to-height", "100"
                ) { result, _ ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.stderr).contains("--to-height must be greater than or equal to --from-height")
                }
    }
}
