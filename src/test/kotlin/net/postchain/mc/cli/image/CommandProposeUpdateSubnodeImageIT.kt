package net.postchain.mc.cli.image

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.postchain.chain0.proposal_subnode_image.proposeUpdateSubnodeImageOperation
import net.postchain.common.hexStringToByteArray
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.compatibility.ApiCompatV95.proposeUpdateSubnodeImageOperationV95
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandProposeUpdateSubnodeImageIT {

    private val digest = "sha256:abcdef123"
    private val url = "gitlab.com/...."
    private val name = "image01"

    @Test
    fun `update subnode image v95`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 95)
                .testCommand(CommandProposeUpdateSubnodeImage(),
                        "--name", name,
                        "--url", url,
                        "--digest", digest,
                        ) { result, api ->
                    assertThat(result.stdout).contains("Subnode image $name update proposed")
                    api.getDcModel().assertCalledOps {
                        it.proposeUpdateSubnodeImageOperationV95(api.pubKey.hexStringToByteArray(), name, url, digest, "Update subnode image $name", null)
                    }
                }
    }

    @Test
    fun `update url v95`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 95)
                .withDCQuery("get_subnode_image", buildGetSubnodeImageResponse(url = url))
                .testCommand(CommandProposeUpdateSubnodeImage(),
                        "--name", name,
                        "--digest", digest,
                        ) { result, api ->
                    assertThat(result.stdout).contains("Subnode image $name update proposed")
                    api.getDcModel().assertCalledOps {
                        it.proposeUpdateSubnodeImageOperationV95(api.pubKey.hexStringToByteArray(), name, url, digest, "Update subnode image $name", null)
                    }
                }
    }

    @Test
    fun `digest is required v95`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 95)
                .withDCQuery("get_subnode_image", buildGetSubnodeImageResponse())
                .testCommand(CommandProposeUpdateSubnodeImage(),
                        "--name", "image01",
                        ) { result, api ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.stderr).contains("Error: missing option --digest (required for API version < 96)")
                }
    }

    @Test
    fun `update subnode image v96 - part 1`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 97)
                .testCommand(CommandProposeUpdateSubnodeImage(),
                        "--name", name,
                        "--url", url,
                        "--digest", digest,
                        "--image-description", "Image version 2"
                        ) { result, api ->
                    assertThat(result.stdout).contains("Subnode image $name update proposed")
                    api.getDcModel().assertCalledOps {
                        it.proposeUpdateSubnodeImageOperation(api.pubKey.hexStringToByteArray(), name, url, digest,
                                "Image version 2",
                                null,
                                null,
                                "Update subnode image $name",
                                null,
                                null)
                    }
                }
    }

    @Test
    fun `update subnode image v96 - part 2`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 97)
                .withDCQuery("get_subnode_image", buildGetSubnodeImageResponse(url = url))
                .testCommand(CommandProposeUpdateSubnodeImage(),
                        "--name", name,
                        "--gtx-modules", "gtx1,gtx2",
                        "--sync-exts", "ext1,ext2",
                        "--base-compute-requests", "1000",
                        "--schedule-at", "2025-07-20 10:11"
                        ) { result, api ->
                    assertThat(result.stdout).contains("Subnode image $name update proposed")
                    api.getDcModel().assertCalledOps {
                        it.proposeUpdateSubnodeImageOperation(api.pubKey.hexStringToByteArray(), name, null, null,
                                null,
                                "gtx1,gtx2",
                                "ext1,ext2",
                                "Update subnode image $name",
                                1753006260000,
                                1000)
                    }
                }
    }

    @Test
    fun `requires options v96`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 97)
                .withDCQuery("get_subnode_image", buildGetSubnodeImageResponse())
                .testCommand(CommandProposeUpdateSubnodeImage(),
                        "--name", "image01",
                        ) { result, api ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.stderr).contains("Error: at least one image data option must be provided (--url, --digest, --image-description, --gtx-modules, --sync-exts, --base-compute-requests)")
                }
    }
}
