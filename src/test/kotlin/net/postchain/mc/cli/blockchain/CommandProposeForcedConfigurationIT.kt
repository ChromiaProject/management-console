package net.postchain.mc.cli.blockchain

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isTrue
import net.postchain.chain0.model.BlockchainState
import net.postchain.common.BlockchainRid
import net.postchain.common.hexStringToByteArray
import net.postchain.mc.cli.test_helpers.DEFAULT_BRID_ECONOMY_CHAIN
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.buildGetBlockchainInfoResponse
import net.postchain.mc.cli.test_helpers.buildGetCompressedConfigurationParts
import net.postchain.mc.cli.test_helpers.writeResourceFileToTempDir
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandProposeForcedConfigurationIT {
    
    private val blockchainRID = BlockchainRid.buildRepeat(10)

    @Test
    fun `propose forced configuration - fail with no height option`(@TempDir dir: Path) {
        val configFile = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir, dcVersion = 80)
                .testCommand(CommandProposeForcedConfiguration(),
                        "--blockchain-config", configFile.absolutePath,
                        "-brid", blockchainRID.toHex(),
                ) { result, _ ->
                    assertThat(result.output).contains("You must specify --height or --detect-height")
                }
    }

    @Test
    fun `propose forced configuration - with height - api v80+`(@TempDir dir: Path) {
        val configFile = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir, dcVersion = 80)
                .withDCQuery("get_compressed_configuration_parts", buildGetCompressedConfigurationParts())
                .testCommand(CommandProposeForcedConfiguration(),
                        "--blockchain-config", configFile.absolutePath,
                        "-brid", blockchainRID.toHex(),
                        "--height", "100",
                        "--resume",
                ) { result, api ->
                    assertThat(result.output).contains("Forced configurations was proposed")
                    assertThat(api.getDcModel().opWasCalled("propose_forced_configuration") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().contentEquals(blockchainRID.data) &&
                                it[2].asByteArray().isNotEmpty() &&
                                it[3].asInteger() == 100L &&
                                it[4].asString() == "Force update of blockchain configuration for ${blockchainRID.toHex()} at height 100" &&
                                it[5].asBoolean()
                    }).isTrue()
                }
    }

    @Test
    fun `propose forced configuration - with height - api v40-79`(@TempDir dir: Path) {
        val configFile = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir, dcVersion = 40)
                .withDCQuery("get_compressed_configuration_parts", buildGetCompressedConfigurationParts())
                .testCommand(CommandProposeForcedConfiguration(),
                        "--blockchain-config", configFile.absolutePath,
                        "-brid", blockchainRID.toHex(),
                        "--height", "100",
                ) { result, api ->
                    assertThat(result.output).contains("Forced configurations was proposed")
                    assertThat(api.getDcModel().opWasCalled("propose_forced_configuration") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().contentEquals(blockchainRID.data) &&
                                it[2].asByteArray().isNotEmpty() &&
                                it[3].asInteger() == 100L &&
                                it[4].asString() == "Force update of blockchain configuration for ${blockchainRID.toHex()} at height 100"
                    }).isTrue()
                }
    }

    @Test
    fun `propose forced configuration - detect height - bc not paused`(@TempDir dir: Path) {
        val configFile = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")

        ManagedRestTestApi(dir, dcVersion = 80)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse())
                .withDCQuery("get_compressed_configuration_parts", buildGetCompressedConfigurationParts())
                .testCommand(CommandProposeForcedConfiguration(),
                        "--blockchain-config", configFile.absolutePath,
                        "-brid", blockchainRID.toHex(),
                        "--detect-height"
                ) { result, api ->
                    assertThat(result.stderr).contains("Error: Blockchain is in state RUNNING but must be PAUSED to detect the height")
                }
    }

    @Test
    fun `propose forced configuration - detect height - chain paused`(@TempDir dir: Path) {
        val configFile = writeResourceFileToTempDir(dir, "/simple_dapp_config.xml")
        ManagedRestTestApi(dir, dcVersion = 80)
                .withDCQuery("get_blockchain_info", buildGetBlockchainInfoResponse(state = BlockchainState.PAUSED))
                .withDCQuery("get_compressed_configuration_parts", buildGetCompressedConfigurationParts())
                .withECModel { it.height = 567 }
                .testCommand(CommandProposeForcedConfiguration(),
                        "--blockchain-config", configFile.absolutePath,
                        "-brid", DEFAULT_BRID_ECONOMY_CHAIN.toHex(),
                        "--detect-height",
                        "--resume",
                ) { result, api ->
                    assertThat(result.output).contains("Forced configurations was proposed")
                    assertThat(api.getDcModel().opWasCalled("propose_forced_configuration") {
                        it[0].asByteArray().contentEquals(api.pubKey.hexStringToByteArray()) &&
                                it[1].asByteArray().contentEquals(api.ecBcRid.data) &&
                                it[2].asByteArray().isNotEmpty() &&
                                it[3].asInteger() == 567L &&
                                it[4].asString() == "Force update of blockchain configuration for ${api.ecBcRid.toHex()} at height 567" &&
                                it[5].asBoolean()
                    }).isTrue()
                }
    }
}