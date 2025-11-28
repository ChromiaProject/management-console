package net.postchain.mc.cli.economy.proposal

import assertk.assertThat
import assertk.assertions.contains
import net.postchain.common.hexStringToByteArray
import net.postchain.common.types.RowId
import net.postchain.economy.common_proposal.makeCommonVoteOperation
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandVoteIT {
    @Test
    fun `vote yes`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .testCommand(CommandVote(),
                        "--id=123",
                        "--accept"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully")
                    api.getEcModel().assertCalledOps {
                        it.makeCommonVoteOperation(api.pubKey.hexStringToByteArray(), RowId(123), true)
                    }
                }
    }

    @Test
    fun `vote no`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .testCommand(CommandVote(),
                        "--id=123",
                        "--reject"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully")
                    api.getEcModel().assertCalledOps {
                        it.makeCommonVoteOperation(api.pubKey.hexStringToByteArray(), RowId(123), false)
                    }
                }
    }

    @Test
    fun `vote multiple proposals`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .testCommand(CommandVote(),
                        "--ids=123,456,789",
                        "--accept"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully for 3 proposals")
                    api.getEcModel().assertCalledOps {
                        it.makeCommonVoteOperation(api.pubKey.hexStringToByteArray(), RowId(123), true)
                        it.makeCommonVoteOperation(api.pubKey.hexStringToByteArray(), RowId(456), true)
                        it.makeCommonVoteOperation(api.pubKey.hexStringToByteArray(), RowId(789), true)
                    }
                }
    }

    @Test
    fun `vote default is yes`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .testCommand(CommandVote(),
                        "--id=123"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully")
                    api.getEcModel().assertCalledOps {
                        it.makeCommonVoteOperation(api.pubKey.hexStringToByteArray(), RowId(123), true)
                    }
                }
    }
}
