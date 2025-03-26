package net.postchain.mc.cli.container

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.postchain.chain0.direct_container.removeContainerOperation
import net.postchain.chain0.proposal_container.proposeRemoveContainerOperation
import net.postchain.common.hexStringToByteArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi.Companion.DEFAULT_PROVIDER_PUBKEY
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandProposeRemoveContainerIT {

    @Test
    fun `propose remove container - success`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 72)
                .testCommand(CommandProposeRemoveContainer(),
                        "--name", "container1",
                        "--proposal",
                ) { result, api ->
                    assertThat(result.output).contains("Container removal proposed")
                    api.getDcModel().assertCalledOps {
                        it.proposeRemoveContainerOperation(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "container1",
                                "Remove container container1"
                        )
                    }
                }
    }

    @Test
    fun `propose remove container - custom description`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 72)
                .testCommand(CommandProposeRemoveContainer(),
                        "--name", "container1",
                        "--proposal",
                        "--description", "Custom container removal description",
                ) { result, api ->
                    assertThat(result.output).contains("Container removal proposed")
                    api.getDcModel().assertCalledOps {
                        it.proposeRemoveContainerOperation(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "container1",
                                "Custom container removal description"
                        )
                    }
                }
    }

    @Test
    fun `direct remove container - fail due to unsupported feature`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 53)
                .withDCQuery("has_direct_container", gtv(false))
                .testCommand(CommandProposeRemoveContainer(),
                        "--name", "container1",
                        "--direct",
                ) { result, _ ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.output).contains("Network does not support direct removal of containers")
                }
    }

    @Test
    fun `direct remove container - success`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 53)
                .withDCQuery("has_direct_container", gtv(true))
                .testCommand(CommandProposeRemoveContainer(),
                        "--name", "container1",
                        "--direct",
                ) { result, api ->
                    assertThat(result.output).contains("Container removed")
                    api.getDcModel().assertCalledOps {
                        it.removeContainerOperation(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "container1"
                        )
                    }
                }
    }
}