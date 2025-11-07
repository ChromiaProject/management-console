package net.postchain.mc.cli.proposal

import assertk.assertThat
import assertk.assertions.contains
import net.postchain.chain0.proposal.voting.makeVoteOperation
import net.postchain.common.hexStringToByteArray
import net.postchain.mc.cli.DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandVoteIT {

    @Test
    fun `vote default - yes`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION - 1)
                .testCommand(CommandVote(),
                        "--id=123"
                        ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully")
                    api.getDcModel().assertCalledOps {
                        it.makeVoteOperation(api.pubKey.hexStringToByteArray(), 123, true)
                    }
                }
    }

    @Test
    fun `vote yes`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION - 1)
                .testCommand(CommandVote(),
                        "--id=123",
                        "--accept",
                        ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully")
                    api.getDcModel().assertCalledOps {
                        it.makeVoteOperation(api.pubKey.hexStringToByteArray(), 123, true)
                    }
                }
    }

    @Test
    fun `vote no`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION - 1)
                .testCommand(CommandVote(),
                        "--id=123",
                        "--reject"
                        ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully")
                    api.getDcModel().assertCalledOps {
                        it.makeVoteOperation(api.pubKey.hexStringToByteArray(), 123, false)
                    }
                }
    }



    @Test
    fun `vote v65 yes`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION)
                .testCommand(CommandVote(),
                        "--id=123",
                        "--accept",
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully")
                    api.getDcModel().assertCalledOps {
                        it.makeVoteOperation(api.pubKey.hexStringToByteArray(), 123, true)
                    }
                }
    }

    @Test
    fun `vote v65 no`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION)
                .testCommand(CommandVote(),
                        "--id=123",
                        "--reject"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully")
                    api.getDcModel().assertCalledOps {
                        it.makeVoteOperation(api.pubKey.hexStringToByteArray(), 123, false)
                    }
                }
    }

    @Test
    fun `vote multiple proposals with ids`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION - 1)
                .testCommand(CommandVote(),
                        "--ids=123,456,789",
                        "--accept"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully for 3 proposals")
                    api.getDcModel().assertCalledOps {
                        it.makeVoteOperation(api.pubKey.hexStringToByteArray(), 123, true)
                        it.makeVoteOperation(api.pubKey.hexStringToByteArray(), 456, true)
                        it.makeVoteOperation(api.pubKey.hexStringToByteArray(), 789, true)
                    }
                }
    }

    @Test
    fun `vote multiple proposals with ids reject`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION - 1)
                .testCommand(CommandVote(),
                        "--ids=100,200",
                        "--reject"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully for 2 proposals")
                    api.getDcModel().assertCalledOps {
                        it.makeVoteOperation(api.pubKey.hexStringToByteArray(), 100, false)
                        it.makeVoteOperation(api.pubKey.hexStringToByteArray(), 200, false)
                    }
                }
    }

    @Test
    fun `vote v65 multiple proposals with ids`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION)
                .testCommand(CommandVote(),
                        "--ids=123,456,789",
                        "--accept"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully for 3 proposals")
                    api.getDcModel().assertCalledOps {
                        it.makeVoteOperation(api.pubKey.hexStringToByteArray(), 123, true)
                        it.makeVoteOperation(api.pubKey.hexStringToByteArray(), 456, true)
                        it.makeVoteOperation(api.pubKey.hexStringToByteArray(), 789, true)
                    }
                }
    }
}
