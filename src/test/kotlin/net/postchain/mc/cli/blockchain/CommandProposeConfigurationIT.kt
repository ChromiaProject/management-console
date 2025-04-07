package net.postchain.mc.cli.blockchain

import assertk.assertThat
import assertk.assertions.isTrue
import net.postchain.chain0.proposal_blockchain.PROPOSE_CONFIGURATION
import net.postchain.chain0.proposal_blockchain.PROPOSE_CONFIGURATION_AT
import net.postchain.common.hexStringToByteArray
import net.postchain.mc.cli.AlreadyExistMode
import net.postchain.mc.cli.test_helpers.DEFAULT_BRID_ECONOMY_CHAIN
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandSuccess
import net.postchain.mc.cli.test_helpers.writeResourceFileToTempDir
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandProposeConfigurationIT {
    @Test
    fun `simple update`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir)
                .withDCQuery("get_compressed_configuration_parts", buildGetCompressedConfigurationParts())
                .testCommand(
                        CommandProposeConfiguration(),
                        "-brid", DEFAULT_BRID_ECONOMY_CHAIN.toHex(),
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                ) { result, api ->
                    assertCommandSuccess(result, "Configuration was proposed")

                    assertThat(api.getDcModel().opWasCalled(PROPOSE_CONFIGURATION) {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().contentEquals(DEFAULT_BRID_ECONOMY_CHAIN.data) &&
                                it[2].asByteArray().isNotEmpty() &&
                                it[3].asString() == "Update of blockchain configuration for ${DEFAULT_BRID_ECONOMY_CHAIN.toHex()}" &&
                                it[4].isNull()
                    }).isTrue()
                }
    }

    @Test
    fun `simple update with height and force`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir)
                .withDCQuery("get_compressed_configuration_parts", buildGetCompressedConfigurationParts())
                .testCommand(
                        CommandProposeConfiguration(),
                        "-brid", DEFAULT_BRID_ECONOMY_CHAIN.toHex(),
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                        "--height", "100",
                        "--force"
                ) { result, api ->
                    assertCommandSuccess(result, "Configuration was proposed")

                    assertThat(api.getDcModel().opWasCalled(PROPOSE_CONFIGURATION_AT) {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().contentEquals(DEFAULT_BRID_ECONOMY_CHAIN.data) &&
                                it[2].asByteArray().isNotEmpty() &&
                                it[3].asInteger() == 100L &&
                                it[4].asBoolean() &&
                                it[5].asString() == "Update of blockchain configuration for ${DEFAULT_BRID_ECONOMY_CHAIN.toHex()} at height 100 with force: ${AlreadyExistMode.FORCE}"
                    }).isTrue()
                }
    }

    @Test
    fun `simple update with blockchain alias`(@TempDir dir: Path) {
        val cityConfig = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir)
                .withDCQuery("get_compressed_configuration_parts", buildGetCompressedConfigurationParts())
                .testCommand(
                        CommandProposeConfiguration(),
                        "-chain", "economy_chain",
                        "--blockchain-config", cityConfig.absolutePath.toString(),
                ) { result, api ->
                    assertCommandSuccess(result, "Configuration was proposed")

                    assertThat(api.getDcModel().opWasCalled(PROPOSE_CONFIGURATION) {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().contentEquals(DEFAULT_BRID_ECONOMY_CHAIN.data) &&
                                it[2].asByteArray().isNotEmpty() &&
                                it[3].asString() == "Update of blockchain configuration for ${DEFAULT_BRID_ECONOMY_CHAIN.toHex()}" &&
                                it[4].isNull()
                    }).isTrue()
                }
    }
}
