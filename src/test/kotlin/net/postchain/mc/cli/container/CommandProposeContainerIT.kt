package net.postchain.mc.cli.container

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.postchain.chain0.direct_container.createContainerFromWithResourceLimitsAndSubnodeImageOperation
import net.postchain.chain0.model.ContainerResourceLimitType
import net.postchain.chain0.proposal_container.proposeContainerOperation
import net.postchain.chain0.proposal_container.proposeContainerWithSubnodeImageOperation
import net.postchain.common.hexStringToByteArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi.Companion.DEFAULT_PROVIDER_PUBKEY
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandProposeContainerIT {

    @Test
    fun `propose container - fail due to dc version`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 49)
                .withDCQuery("has_direct_container", gtv(false))
                .testCommand(CommandProposeContainer(),
                        "--cluster", "cluster1",
                        "--name", "container1",
                        "--voter-set", "vs1",
                ) { result, _ ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.output).contains("Direct container creation is not supported")
                }
    }

    @Test
    fun `propose container - success`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("has_direct_container", gtv(true))
                .testCommand(CommandProposeContainer(),
                        "--cluster", "cluster1",
                        "--name", "container1",
                        "--voter-set", "vs1",
                        "--consensus-threshold", "2",
                        "--container-units", "2",
                        "--max-blockchains", "5",
                        "--extra-storage", "100",
                ) { result, api ->
                    assertThat(result.output).contains("Container container1 has been created")
                    api.getDcModel().assertCalledOps {
                        it.createContainerFromWithResourceLimitsAndSubnodeImageOperation(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "container1",
                                "cluster1",
                                2,
                                "vs1",
                                mapOf(
                                        ContainerResourceLimitType.container_units to 2,
                                        ContainerResourceLimitType.max_blockchains to 5,
                                        ContainerResourceLimitType.extra_storage to 100
                                ),
                                ""
                        )
                    }
                }
    }

    @Test
    fun `propose container - pubkey not valid`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("has_direct_container", gtv(false))
                .withDCQuery("get_provider_by_key", gtv(DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray()))
                .testCommand(CommandProposeContainer(),
                        "--cluster", "cluster1",
                        "--name", "container1",
                        "--proposal",
                        "--pubkeys", DEFAULT_PROVIDER_PUBKEY,
                ) { result, _ ->
                    assertThat(result.output).contains("Container proposals does not support specifying public keys as deployer")
                }
    }

    @Test
    fun `propose container - image - not valid in version below 57`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 56)
                .withDCQuery("has_direct_container", gtv(false))
                .withDCQuery("get_provider_by_key", gtv(DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray()))
                .testCommand(CommandProposeContainer(),
                        "--cluster", "cluster1",
                        "--name", "container1",
                        "--voter-set", "vs1",
                        "--proposal",
                        "--subnode-image-name=image-url",
                ) { result, _ ->
                    assertThat(result.output).contains("Setting subnode image for container is not supported by network")
                }
    }

    @Test
    fun `propose container - image - success`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 57)
                .withDCQuery("has_direct_container", gtv(false))
                .withDCQuery("get_provider_by_key", gtv(DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray()))
                .testCommand(CommandProposeContainer(),
                        "--cluster", "cluster1",
                        "--name", "container1",
                        "--voter-set", "vs1",
                        "--proposal",
                        "--subnode-image-name=image-url",
                ) { _, api ->
                    api.getDcModel().assertCalledOps {
                        it.proposeContainerWithSubnodeImageOperation(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "cluster1",
                                "container1",
                                "vs1",
                                "image-url",
                                "Add container container1 to the cluster cluster1 - consensus-threshold: 0, voter-set: vs1, container-units: 1, max-blockchains: 10, extra-storage: 0"
                        )
                    }
                }
    }

    @Test
    fun `propose container - success with voter set`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 72)
                .withDCQuery("has_direct_container", gtv(false))
                .withDCQuery("get_provider_by_key", gtv(DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray()))
                .testCommand(CommandProposeContainer(),
                        "--cluster", "cluster1",
                        "--name", "container1",
                        "--voter-set", "vs1",
                        "--proposal",
                        "--description", "New container"
                ) { result, api ->
                    assertThat(result.output).contains("Proposing container")
                    api.getDcModel().assertCalledOps {
                        it.proposeContainerOperation(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "cluster1",
                                "container1",
                                "vs1",
                                "New container"
                        )
                    }
                }
    }
}
