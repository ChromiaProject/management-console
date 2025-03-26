package net.postchain.mc.cli.blockchain

import assertk.assertThat
import assertk.assertions.contains
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandOutput
import net.postchain.mc.cli.test_helpers.buildGetBlockchainInfoListResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandListBlockchainsIT {

    @Test
    fun `simple list`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info_list", buildGetBlockchainInfoListResponse())
                .testCommand(CommandListBlockchains(), "--includeinactive",
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
                                "Cluster": "cluster01"
                                }
                                ]
                            """.trimIndent()
                    )
                }
    }

    @Test
    fun `interactive requires v17`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 16)
                .testCommand(CommandListBlockchains(),
                        "--interactive",
                ) { result, _ ->
                    assertThat(result.stderr).contains("--interactive requires directory chain version 17, found version 16")
                }
    }

}