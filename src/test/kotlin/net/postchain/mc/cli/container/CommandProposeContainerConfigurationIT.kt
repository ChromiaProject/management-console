package net.postchain.mc.cli.container

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.postchain.chain0.proposal_container.proposal_container_configuration.ContainerConfigurationData
import net.postchain.chain0.proposal_container.proposal_container_configuration.proposeContainerConfigurationOperation
import net.postchain.common.hexStringToByteArray
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi.Companion.DEFAULT_PROVIDER_PUBKEY
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandProposeContainerConfigurationIT {
    @Test
    fun `propose container configuration - no configuration specified`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 101)
                .testCommand(
                        CommandProposeContainerConfiguration(),
                        "--name", "container1",
                ) { result, _ ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.output).contains("No configurations are specified")
                }
    }

    @Test
    fun `propose container configuration - with slow db statement log`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 101)
                .testCommand(
                        CommandProposeContainerConfiguration(),
                        "--name", "container1",
                        "--slow-db-statement-log-ms", "500",
                ) { result, api ->
                    assertThat(result.output).contains("Container configuration proposed")
                    api.getDcModel().assertCalledOps {
                        it.proposeContainerConfigurationOperation(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "container1",
                                ContainerConfigurationData(500),
                                "Update container configuration for container1 - slow-db-statement-log-ms: 500"
                        )
                    }
                }
    }
}
