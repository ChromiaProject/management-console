package net.postchain.mc.cli.blockchain.move

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.github.ajalt.clikt.testing.test
import net.postchain.chain0.model.BlockchainState
import net.postchain.common.BlockchainRid
import net.postchain.common.hexStringToByteArray
import net.postchain.gtv.GtvNull
import net.postchain.mc.cli.blockchain.buildGetBlockchainInfoResponse
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandFailureContains
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandProposeBlockchainMoveIT {

    private val testBcRid = BlockchainRid.buildRepeat(10)

    @Test
    fun `validate arguments`(@TempDir dir: Path) {
        var result = CommandProposeBlockchainMove().test(argv = listOf(
                "-brid", testBcRid.toHex()
        ))
        assertCommandFailureContains(result, "Error: missing option --destination-container")

        result = CommandProposeBlockchainMove().test(argv = listOf(
                "-dc", "container02"
        ))
        assertCommandFailureContains(result, "Error: missing option --blockchain-rid")
    }

    @Test
    fun `successful move proposal`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(
                        state = BlockchainState.RUNNING,
                        rid = testBcRid
                ))
                .testCommand(
                        CommandProposeBlockchainMove(),
                        "-brid", testBcRid.toHex(),
                        "-dc", "container02"
                ) { result, api ->
                    assertCommandSuccessContains(result, "Destination cluster nodes will begin syncing blockchain ${testBcRid.toHex()} to container container02 once the proposal is approved")
                    assertCommandSuccessContains(result, "Blockchain move proposed")

                    assertThat(api.getDcModel().opWasCalled("propose_blockchain_move") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().contentEquals(testBcRid.data) &&
                                it[2].asString() == "container02" &&
                                it[3].asString() == "Move blockchain ${testBcRid.toHex()} to the container container02" &&
                                it[4].asBoolean()
                    }).isTrue()
                }
    }

    @Test
    fun `successful move proposal with custom description`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(
                        state = BlockchainState.RUNNING,
                        rid = testBcRid
                ))
                .testCommand(
                        CommandProposeBlockchainMove(),
                        "-brid", testBcRid.toHex(),
                        "-dc", "container02",
                        "--description", "Custom move description"
                ) { result, api ->
                    assertCommandSuccessContains(result, "Blockchain move proposed")

                    assertThat(api.getDcModel().opWasCalled("propose_blockchain_move") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().contentEquals(testBcRid.data) &&
                                it[2].asString() == "container02" &&
                                it[3].asString() == "Custom move description" &&
                                it[4].asBoolean()
                    }).isTrue()
                }
    }

    @Test
    fun `paused blockchain shows warning in interactive mode - user confirms`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(
                        state = BlockchainState.PAUSED,
                        rid = testBcRid
                ))
                .testInteractiveCommand(
                        CommandProposeBlockchainMove(),
                        "y\n",
                        "-brid", testBcRid.toHex(),
                        "-dc", "container02"
                ) { result, api ->
                    assertThat(result.output).contains("WARNING: Blockchain is PAUSED")
                    assertThat(result.output).contains("Move may take time and blockchain cannot be resumed until the move is finished")
                    assertCommandSuccessContains(result, "Blockchain move proposed")

                    assertThat(api.getDcModel().opWasCalled("propose_blockchain_move") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().contentEquals(testBcRid.data) &&
                                it[2].asString() == "container02" &&
                                it[4].asBoolean()
                    }).isTrue()
                }
    }

    @Test
    fun `paused blockchain shows warning in interactive mode - user cancels`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(
                        state = BlockchainState.PAUSED,
                        rid = testBcRid
                ))
                .testInteractiveCommand(
                        CommandProposeBlockchainMove(),
                        "n\n",
                        "-brid", testBcRid.toHex(),
                        "-dc", "container02"
                ) { result, api ->
                    assertThat(result.output).contains("WARNING: Blockchain is ${BlockchainState.PAUSED}")
                    assertCommandFailureContains(result, "Canceled")

                    assertThat(api.getDcModel().opWasCalled("propose_blockchain_move") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray())
                    }).isEqualTo(false)
                }
    }

    @Test
    fun `blockchain not found in interactive mode`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info", GtvNull)
                .testInteractiveCommand(
                        CommandProposeBlockchainMove(),
                        listOf(),
                        "-brid", testBcRid.toHex(),
                        "-dc", "container02"
                ) { result, api ->
                    assertCommandFailureContains(result, "Blockchain not found")
                }
    }

    @Test
    fun `yes flag skips confirmation for paused blockchain`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(
                        state = BlockchainState.PAUSED,
                        rid = testBcRid
                ))
                .testCommand(
                        CommandProposeBlockchainMove(),
                        "-brid", testBcRid.toHex(),
                        "-dc", "container02",
                        "--yes"
                ) { result, api ->
                    assertCommandSuccessContains(result, "Blockchain move proposed")

                    assertThat(api.getDcModel().opWasCalled("propose_blockchain_move") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().contentEquals(testBcRid.data) &&
                                it[2].asString() == "container02" &&
                                it[4].asBoolean()
                    }).isTrue()
                }
    }

    @Test
    fun `no-keep-src-replica flag passes false for keepSrcReplica`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(
                        state = BlockchainState.RUNNING,
                        rid = testBcRid
                ))
                .testCommand(
                        CommandProposeBlockchainMove(),
                        "-brid", testBcRid.toHex(),
                        "-dc", "container02",
                        "--no-keep-src-replica"
                ) { result, api ->
                    assertCommandSuccessContains(result, "Blockchain move proposed")

                    assertThat(api.getDcModel().opWasCalled("propose_blockchain_move") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().contentEquals(testBcRid.data) &&
                                it[2].asString() == "container02" &&
                                !it[4].asBoolean()
                    }).isTrue()
                }
    }
}
