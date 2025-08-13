package net.postchain.mc.cli.blockchain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.github.ajalt.clikt.testing.test
import net.postchain.common.hexStringToByteArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.test_helpers.DEFAULT_DAPP_RID
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.addDcEmptyListQueries
import net.postchain.mc.cli.test_helpers.assertCommandFailureContains
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import net.postchain.mc.cli.test_helpers.writeResourceFileToTempDir
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.Instant

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
}
