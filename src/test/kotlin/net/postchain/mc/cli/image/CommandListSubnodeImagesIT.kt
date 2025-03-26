package net.postchain.mc.cli.image

import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandContains
import net.postchain.mc.cli.test_helpers.buildGetSubnodeImagesResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandListSubnodeImagesIT {

    @Test
    fun `direct container - fail due to dc version`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_subnode_images", buildGetSubnodeImagesResponse())
                .testCommand(CommandListSubnodeImages()) { result, _ ->
                    assertCommandContains(result.stdout, """
                        {
                          "Name": "image01",
                          "URL": "gitlab.com/....",
                          "Digest": "sha256:abcdef123",
                          "Type": "COMMON",
                          "Owner": "",
                          "Description": "Description of image01",
                          "Active": "true"
                        }
                        """.trimIndent())
                }
    }
}
