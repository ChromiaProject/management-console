package net.postchain.mc.cli.image

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.postchain.chain0.proposal_subnode_image.proposeUpdateSubnodeImageOperation
import net.postchain.common.hexStringToByteArray
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandProposeUpdateSubnodeImageIT {

    private val digest = "sha256:abcdef123"
    private val url = "gitlab.com/...."
    private val name = "image01"

    @Test
    fun `update subnode image`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .testCommand(CommandProposeUpdateSubnodeImage(),
                        "--name", name,
                        "--url", url,
                        "--digest", digest,
                        ) { result, api ->
                    assertThat(result.stdout).contains("Subnode image $name update proposed")
                    api.getDcModel().assertCalledOps {
                        it.proposeUpdateSubnodeImageOperation(api.pubKey.hexStringToByteArray(), name, url, digest, "Update subnode image $name with URL $url and digest $digest")
                    }
                }
    }

    @Test
    fun `resolve url`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_subnode_image", buildGetSubnodeImageResponse(url = url))
                .testCommand(CommandProposeUpdateSubnodeImage(),
                        "--name", name,
                        "--digest", digest,
                        ) { result, api ->
                    assertThat(result.stdout).contains("Subnode image $name update proposed")
                    api.getDcModel().assertCalledOps {
                        it.proposeUpdateSubnodeImageOperation(api.pubKey.hexStringToByteArray(), name, url, digest, "Update subnode image $name with URL $url and digest $digest")
                    }
                }
    }

    @Test
    fun `digest is required`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_subnode_image", buildGetSubnodeImageResponse())
                .testCommand(CommandProposeUpdateSubnodeImage(),
                        "--name", "image01",
                        ) { result, api ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.stderr).contains("Error: missing option --digest")
                }
    }
}
