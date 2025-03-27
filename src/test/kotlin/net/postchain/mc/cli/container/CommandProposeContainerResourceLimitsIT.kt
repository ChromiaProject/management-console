package net.postchain.mc.cli.container

import assertk.assertThat
import assertk.assertions.contains
import net.postchain.chain0.model.ContainerResourceLimitType
import net.postchain.chain0.proposal_container.proposal_container_limits.proposeContainerLimitsOperation
import net.postchain.common.hexStringToByteArray
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi.Companion.DEFAULT_PROVIDER_PUBKEY
import net.postchain.mc.compatibility.ApiCompatV22
import net.postchain.mc.compatibility.ApiCompatV22.proposeContainerLimitsOperationV22
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandProposeContainerResourceLimitsIT {

    @Test
    fun `propose container limits - no resource limits specified`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 72)
                .testCommand(CommandProposeContainerResourceLimits(),
                        "--name", "container1",
                ) { result, _ ->
                    assertThat(result.output).contains("No resource limits are specified")
                }
    }

    @Test
    fun `propose container limits - success with container units and max blockchains - dcVersion 24+`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 24)
                .testCommand(CommandProposeContainerResourceLimits(),
                        "--name", "container1",
                        "--container-units", "2",
                        "--max-blockchains", "5",
                        "--extra-storage", "100",
                ) { result, api ->
                    assertThat(result.output).contains("Container limits proposed")
                    api.getDcModel().assertCalledOps {
                        it.proposeContainerLimitsOperation(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "container1",
                                mapOf(
                                        ContainerResourceLimitType.container_units to 2,
                                        ContainerResourceLimitType.max_blockchains to 5,
                                        ContainerResourceLimitType.extra_storage to 100
                                ),
                                "Update container resource limits for container1 - container-units: 2, max-blockchains: 5, extra-storage: 100 (MiB)"
                        )
                    }
                }
    }

    @Test
    fun `propose container limits - success with container units only - dcVersion 3-23`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 3)
                .testCommand(CommandProposeContainerResourceLimits(),
                        "--name", "container1",
                        "--container-units", "2",
                ) { result, api ->
                    assertThat(result.output).contains("Container limits proposed")
                    api.getDcModel().assertCalledOps {
                        it.proposeContainerLimitsOperationV22(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "container1",
                                mapOf(
                                        ApiCompatV22.ContainerResourceLimitType.container_units to 2
                                ),
                                "Update container resource limits for container1 - container-units: 2, max-blockchains: null, extra-storage: null (MiB)"
                        )
                    }
                }
    }
}