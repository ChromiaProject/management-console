package net.postchain.mc.cli.image

import assertk.assertThat
import assertk.assertions.contains
import net.postchain.chain0.proposal_subnode_image.proposeAddClusterSubnodeImageOperation
import net.postchain.common.hexStringToByteArray
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandProposeAddSubnodeImageToClusterIT {

    @Test
    fun `add subnode image`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .testCommand(CommandProposeAddSubnodeImageToCluster(),
                        "--cluster-name", "cluster01",
                        "--subnode-image-name", "image01"
                        ) { result, api ->
                    assertThat(result.stdout).contains("Adding image01 to cluster01 proposed")
                    api.getDcModel().assertCalledOps {
                        it.proposeAddClusterSubnodeImageOperation(api.pubKey.hexStringToByteArray(), "cluster01", "image01", "Add image01 to cluster01")
                    }
                }
    }
}
