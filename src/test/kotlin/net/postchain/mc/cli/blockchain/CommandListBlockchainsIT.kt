package net.postchain.mc.cli.blockchain

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandOutput
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandListBlockchainsIT {

    @Test
    fun `simple list`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info_list", buildGetBlockchainInfoListResponse())
                .testCommand(
                        CommandListBlockchains(), "--includeinactive",
                ) { result, _ ->
                    assertCommandOutput(result.stdout,
                            """
                                [
                                {
                                  "Name": "bc01",
                                  "Rid": "0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A",
                                  "State": "RUNNING",
                                  "Container": "container01",
                                  "Cluster": "cluster01"
                                },
                                {
                                  "Name": "bc02",
                                  "Rid": "0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B",
                                  "State": "RUNNING",
                                  "Container": "container02",
                                  "Cluster": "cluster01"
                                },
                                {
                                  "Name": "bc03",
                                  "Rid": "0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A",
                                  "State": "PAUSED",
                                  "Container": "container03",
                                  "Cluster": "cluster03"
                                }
                                ]
                            """.trimIndent()
                    )
                }
    }

    @Test
    fun `interactive requires v17`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 16)
                .testCommand(
                        CommandListBlockchains(),
                        "--interactive",
                ) { result, _ ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.stderr).contains("--interactive requires directory chain version 17, found version 16")
                }
    }

    @Test
    fun `filter by cluster returns matching blockchains`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info_list", buildGetBlockchainInfoListResponse())
                .testCommand(CommandListBlockchains(),
                        "--includeinactive",
                        "--cluster", "cluster01"
                ) { result, _ ->
                    assertCommandOutput(result.stdout,
                            """
                                [
                                {
                                  "Name": "bc01",
                                  "Rid": "0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A",
                                  "State": "RUNNING",
                                  "Container": "container01",
                                  "Cluster": "cluster01"
                                },
                                {
                                  "Name": "bc02",
                                  "Rid": "0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B",
                                  "State": "RUNNING",
                                  "Container": "container02",
                                  "Cluster": "cluster01"
                                }
                                ]
                            """.trimIndent()
                    )
                }
    }

    @Test
    fun `filter by non-existent cluster returns empty`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info_list", buildGetBlockchainInfoListResponse())
                .testCommand(CommandListBlockchains(),
                        "--includeinactive",
                        "--cluster", "nonexistent"
                ) { result, _ ->
                    assertCommandOutput(result.stdout, "[]")
                }
    }

    @Test
    fun `filter by container returns matching blockchains`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info_list", buildGetBlockchainInfoListResponse())
                .testCommand(CommandListBlockchains(),
                        "--includeinactive",
                        "--container", "container01"
                ) { result, _ ->
                    assertCommandOutput(result.stdout,
                            """
                                [
                                {
                                  "Name": "bc01",
                                  "Rid": "0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A",
                                  "State": "RUNNING",
                                  "Container": "container01",
                                  "Cluster": "cluster01"
                                }
                                ]
                            """.trimIndent()
                    )
                }
    }

    @Test
    fun `filter by partial container name matches all containers`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info_list", buildGetBlockchainInfoListResponse())
                .testCommand(CommandListBlockchains(),
                        "--includeinactive",
                        "--container", "ner02"
                ) { result, _ ->
                    assertCommandOutput(result.stdout,
                            """
                                [
                                {
                                  "Name": "bc02",
                                  "Rid": "0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B",
                                  "State": "RUNNING",
                                  "Container": "container02",
                                  "Cluster": "cluster01"
                                }
                                ]
                            """.trimIndent()
                    )
                }
    }

    @Test
    fun `filter by container suffix matches subset`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info_list", buildGetBlockchainInfoListResponse())
                .testCommand(CommandListBlockchains(),
                        "--includeinactive",
                        "--container", "02"
                ) { result, _ ->
                    assertThat(result.stdout).contains("bc02")
                    assertThat(result.stdout).contains("container02")
                }
    }

    @Test
    fun `filter by non-existent container returns empty`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info_list", buildGetBlockchainInfoListResponse())
                .testCommand(CommandListBlockchains(),
                        "--includeinactive",
                        "--container", "nonexistent"
                ) { result, _ ->
                    assertCommandOutput(result.stdout, "[]")
                }
    }

    @Test
    fun `filter by cluster and container returns matching blockchains`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info_list", buildGetBlockchainInfoListResponse())
                .testCommand(CommandListBlockchains(),
                        "--includeinactive",
                        "--cluster", "cluster01",
                        "--container", "container02"
                ) { result, _ ->
                    assertCommandOutput(result.stdout,
                            """
                                [
                                {
                                  "Name": "bc02",
                                  "Rid": "0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B0B",
                                  "State": "RUNNING",
                                  "Container": "container02",
                                  "Cluster": "cluster01"
                                }
                                ]
                            """.trimIndent()
                    )
                }
    }

    @Test
    fun `filter by cluster and non-matching container returns empty`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info_list", buildGetBlockchainInfoListResponse())
                .testCommand(CommandListBlockchains(),
                        "--includeinactive",
                        "--cluster", "cluster01",
                        "--container", "nonexistent"
                ) { result, _ ->
                    assertCommandOutput(result.stdout, "[]")
                }
    }

}