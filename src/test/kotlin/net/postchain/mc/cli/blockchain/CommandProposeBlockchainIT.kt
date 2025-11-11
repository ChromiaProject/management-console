package net.postchain.mc.cli.blockchain

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.chromia.build.tools.multisignature.MultiSignatureTxData
import com.github.ajalt.clikt.testing.test
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtx.Gtx
import net.postchain.mc.cli.test_helpers.DEFAULT_BRID_DIRECTORY_CHAIN
import net.postchain.mc.cli.test_helpers.DEFAULT_DAPP_RID
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.addDcEmptyListQueries
import net.postchain.mc.cli.test_helpers.assertCommandFailureContains
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import net.postchain.mc.cli.test_helpers.writeResourceFileToTempDir
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.absolutePathString
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

class CommandProposeBlockchainIT {

    @Test
    fun `validate arguments`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        var result = CommandProposeBlockchain().test(argv = listOf(
                "-bc", cityConfig.absolutePath.toString()
        ))
        assertCommandFailureContains(result, "Error: missing option --container")
        assertCommandFailureContains(result, "Error: missing option --name")

        result = CommandProposeBlockchain().test(argv = listOf(
                "--name", "name01"
        ))
        assertCommandFailureContains(result, "Error: missing option --container")
        assertCommandFailureContains(result, "Error: missing option --blockchain-config")
    }

    @Test
    fun `successful add`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir)
                .addDcEmptyListQueries("get_compressed_configuration_parts")
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testCommand(
                        CommandProposeBlockchain(),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                ) { result, api ->
                    assertCommandSuccessContains(result, "Blockchain name01 has been added, bc-rid: ${DEFAULT_DAPP_RID.toHex()}")

                    assertThat(api.getDcModel().opWasCalled("propose_blockchain") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().isNotEmpty() &&
                                it[2].asString() == "name01" &&
                                it[3].asString() == "container01" &&
                                it[4].asString() == "Add blockchain name01 to the container container01"
                    }).isTrue()
                }
    }

    @Test
    fun `successful add quiet`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir)
                .addDcEmptyListQueries("get_compressed_configuration_parts")
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testCommand(
                        CommandProposeBlockchain(),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                        "-q"
                ) { result, api ->
                    assertThat(result.statusCode).isEqualTo(0)
                    assertThat(result.stdout.trim()).isEqualTo(DEFAULT_DAPP_RID.toHex())

                    assertThat(api.getDcModel().opWasCalled("propose_blockchain") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().isNotEmpty() &&
                                it[2].asString() == "name01" &&
                                it[3].asString() == "container01" &&
                                it[4].asString() == "Add blockchain name01 to the container container01"
                    }).isTrue()
                }
    }

    @Test
    fun `successful add with timeb at`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        val timeb = Instant.now().plusSeconds(60).toEpochMilli()
        ManagedRestTestApi(dir)
                .addDcEmptyListQueries("get_compressed_configuration_parts")
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testCommand(
                        CommandProposeBlockchain(),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                        "--timeb-at", timeb.toString(),
                ) { result, api ->
                    assertCommandSuccessContains(result, "Blockchain name01 has been added, bc-rid: ${DEFAULT_DAPP_RID.toHex()}")

                    assertThat(api.getDcModel().opWasCalled("timeb") {
                        it[0].asInteger() == 0L && it[1].asInteger() == timeb
                    }).isTrue()

                    assertThat(api.getDcModel().opWasCalled("propose_blockchain") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().isNotEmpty() &&
                                it[2].asString() == "name01" &&
                                it[3].asString() == "container01" &&
                                it[4].asString() == "Add blockchain name01 to the container container01"
                    }).isTrue()
                }
    }

    @Test
    fun `successful add with timeb after`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir)
                .addDcEmptyListQueries("get_compressed_configuration_parts")
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testCommand(
                        CommandProposeBlockchain(),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                        "--timeb-after", "60",
                ) { result, api ->
                    assertCommandSuccessContains(result, "Blockchain name01 has been added, bc-rid: ${DEFAULT_DAPP_RID.toHex()}")

                    assertThat(api.getDcModel().opWasCalled("timeb") {
                        it[0].asInteger() == 0L
                    }).isTrue()

                    assertThat(api.getDcModel().opWasCalled("propose_blockchain") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().isNotEmpty() &&
                                it[2].asString() == "name01" &&
                                it[3].asString() == "container01" &&
                                it[4].asString() == "Add blockchain name01 to the container container01"
                    }).isTrue()
                }
    }

    @Test
    fun `successful save transaction to file`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        val signers = writeResourceFileToTempDir(dir, "/signers")
        ManagedRestTestApi(dir)
                .addDcEmptyListQueries("get_compressed_configuration_parts")
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testCommand(
                        CommandProposeBlockchain(),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                        "--signers-file", signers.absolutePath.toString(),
                        "--target", dir.absolutePathString(),
                ) { result, api ->
                    assertCommandSuccessContains(result, "Transaction is written as hex to file: ")
                    val savedTransactionData = dir.listDirectoryEntries("transaction_*").single().readText()
                    val savedTransaction = MultiSignatureTxData.decode(savedTransactionData)
                    val gtx = Gtx.decode(savedTransaction.transaction)
                    assertThat(gtx.gtxBody.blockchainRid).isEqualTo(DEFAULT_BRID_DIRECTORY_CHAIN)
                    assertThat(gtx.gtxBody.signers.map { it.toHex() }).containsExactlyInAnyOrder(
                            DEFAULT_PROVIDER01_PUBKEY,
                            "03694B937C7059C4CE28327BBD4E98E2BC8E7D34715A699391F019A6B17320000A",
                            "02B45DF3AF9FDBBAE931D9492842E332E76B5D07F23893033AA27C87623B574134",
                    )

                    assertThat(api.getDcModel().capturedOps).isEmpty()
                }
    }
}
