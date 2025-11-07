package net.postchain.mc.cli.economy.proposal

import assertk.assertThat
import assertk.assertions.contains
import net.postchain.common.hexStringToByteArray
import net.postchain.common.types.RowId
import net.postchain.crypto.PubKey
import net.postchain.economy.common_proposal.makeCommonVoteOperation
import net.postchain.mc.cli.base.ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.compatibility.ApiCompatECV21.makeVoteOperationECV20
import net.postchain.mc.compatibility.ApiCompatECV57.makeCommonVoteOperation as makeCommonVoteOperationCompat
import net.postchain.mc.compatibility.ApiCompatECV57.makeCommonVoteV65Operation
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandVoteIT {

    // Tests for EC version < 21 (ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION)
    @Test
    fun `vote yes - ECV20`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION - 1)
                .testCommand(CommandVote(),
                        "--id=123",
                        "--accept"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully")
                    api.getEcModel().assertCalledOps {
                        it.makeVoteOperationECV20(api.pubKey.hexStringToByteArray(), RowId(123), true)
                    }
                }
    }

    @Test
    fun `vote no - ECV20`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION - 1)
                .testCommand(CommandVote(),
                        "--id=123",
                        "--reject"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully")
                    api.getEcModel().assertCalledOps {
                        it.makeVoteOperationECV20(api.pubKey.hexStringToByteArray(), RowId(123), false)
                    }
                }
    }

    @Test
    fun `vote multiple proposals - ECV20`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION - 1)
                .testCommand(CommandVote(),
                        "--ids=123,456,789",
                        "--accept"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully for 3 proposals")
                    api.getEcModel().assertCalledOps {
                        it.makeVoteOperationECV20(api.pubKey.hexStringToByteArray(), RowId(123), true)
                        it.makeVoteOperationECV20(api.pubKey.hexStringToByteArray(), RowId(456), true)
                        it.makeVoteOperationECV20(api.pubKey.hexStringToByteArray(), RowId(789), true)
                    }
                }
    }

    // Tests for EC version >= 21 and < 43 (ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION)
    @Test
    fun `vote yes - ECV21 to ECV42`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION)
                .testCommand(CommandVote(),
                        "--id=123",
                        "--accept"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully")
                    api.getEcModel().assertCalledOps {
                        it.makeCommonVoteOperationCompat(PubKey(api.pubKey.hexStringToByteArray()), RowId(123), true)
                    }
                }
    }

    @Test
    fun `vote no - ECV21 to ECV42`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION)
                .testCommand(CommandVote(),
                        "--id=123",
                        "--reject"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully")
                    api.getEcModel().assertCalledOps {
                        it.makeCommonVoteOperationCompat(PubKey(api.pubKey.hexStringToByteArray()), RowId(123), false)
                    }
                }
    }

    @Test
    fun `vote multiple proposals - ECV21 to ECV42`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION)
                .testCommand(CommandVote(),
                        "--ids=100,200,300",
                        "--accept"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully for 3 proposals")
                    api.getEcModel().assertCalledOps {
                        it.makeCommonVoteOperationCompat(PubKey(api.pubKey.hexStringToByteArray()), RowId(100), true)
                        it.makeCommonVoteOperationCompat(PubKey(api.pubKey.hexStringToByteArray()), RowId(200), true)
                        it.makeCommonVoteOperationCompat(PubKey(api.pubKey.hexStringToByteArray()), RowId(300), true)
                    }
                }
    }

    // Tests for EC version >= 43 and < 57 (ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION)
    @Test
    fun `vote yes - ECV43 to ECV56`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION)
                .testCommand(CommandVote(),
                        "--id=123",
                        "--accept"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully")
                    api.getEcModel().assertCalledOps {
                        it.makeCommonVoteV65Operation(RowId(123), true)
                    }
                }
    }

    @Test
    fun `vote no - ECV43 to ECV56`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION)
                .testCommand(CommandVote(),
                        "--id=123",
                        "--reject"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully")
                    api.getEcModel().assertCalledOps {
                        it.makeCommonVoteV65Operation(RowId(123), false)
                    }
                }
    }

    @Test
    fun `vote multiple proposals - ECV43 to ECV56`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION)
                .testCommand(CommandVote(),
                        "--ids=111,222",
                        "--reject"
                ) { result, api ->
                    assertThat(result.stdout).contains("Vote added successfully for 2 proposals")
                    api.getEcModel().assertCalledOps {
                        it.makeCommonVoteV65Operation(RowId(111), false)
                        it.makeCommonVoteV65Operation(RowId(222), false)
                    }
                }
    }

    // Tests for EC version >= 57 (latest)
    @Test
    fun `vote yes - ECV57 and above`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION)
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
    fun `vote no - ECV57 and above`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION)
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
    fun `vote multiple proposals - ECV57 and above`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION)
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
    fun `vote default is yes - ECV57 and above`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION)
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
