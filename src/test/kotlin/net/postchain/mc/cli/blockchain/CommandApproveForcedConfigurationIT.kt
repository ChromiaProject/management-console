package net.postchain.mc.cli.blockchain

import assertk.assertThat
import assertk.assertions.isTrue
import net.postchain.chain0.proposal_blockchain.APPROVE_PROPOSED_FORCED_CONFIGURATION
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandSuccess
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandApproveForcedConfigurationIT {

    private val blockchainRID = BlockchainRid.buildRepeat(10)

    @Test
    fun `approve forced configuration`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 85)
                .testCommand(
                        CommandApproveForcedConfiguration(),
                        "-brid", blockchainRID.toHex(),
                ) { result, api ->
                    assertCommandSuccess(result, "Forced configurations was approved")
                    assertThat(api.getDcModel().opWasCalled(APPROVE_PROPOSED_FORCED_CONFIGURATION) {
                        it[1].asByteArray().contentEquals(blockchainRID.data)
                    }).isTrue()
                }
    }
}
