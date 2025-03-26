package net.postchain.mc.cli.blockchain

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.github.ajalt.clikt.testing.test
import net.postchain.common.hexStringToByteArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.test_helpers.DEFAULT_DAPP_RID
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.buildGetCompressedConfigurationParts
import net.postchain.mc.cli.test_helpers.writeResourceFileToTempDir
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandProposeBlockchainIT {

    @Test
    fun `validate arguments`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        var result = CommandProposeBlockchain().test(argv = listOf(
                "-bc", cityConfig.absolutePath.toString()
        ))
        assertThat(result.stderr).contains("Error: missing option --container")
        assertThat(result.stderr).contains("Error: missing option --name")

        result = CommandProposeBlockchain().test(argv = listOf(
                "--name", "name01"
        ))
        assertThat(result.stderr).contains("Error: missing option --container")
        assertThat(result.stderr).contains("Error: missing option --blockchain-config")
    }

    @Test
    fun `successful add`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir)
                .withDCQuery("get_compressed_configuration_parts", buildGetCompressedConfigurationParts())
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testCommand(
                        CommandProposeBlockchain(),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                ) { result, api ->
                    assertThat(result.stdout).contains("Blockchain name01 has been added, bc-rid: ${DEFAULT_DAPP_RID.toHex()}")

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
                .withDCQuery("get_compressed_configuration_parts", buildGetCompressedConfigurationParts())
                .withDCQuery("find_blockchain_rid", gtv(DEFAULT_DAPP_RID))
                .testCommand(
                        CommandProposeBlockchain(),
                        "-c", "container01",
                        "-n", "name01",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                        "-q"
                ) { result, api ->
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
}