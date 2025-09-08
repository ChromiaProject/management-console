package net.postchain.mc.cli.blockchain.import_chain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import net.postchain.common.hexStringToByteArray
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.writeResourceFileToTempDir
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandProposeFinishBlockchainImportTestIT {

    @Test
    fun `successful creating finish import blockchain proposal if all configurations are found`(@TempDir dir: Path) {
        val config = writeResourceFileToTempDir(dir, "/city.configs")
        val configurationHeights = mapOf<Long, Gtv>(0L to gtv(33L), 33L to GtvNull)
        buildTest(dir, configurationHeights)
                .testCommand(CommandProposeFinishBlockchainImport(),
                        "--configurations-file", config.absolutePath, "--final-height", "100"
                ) { result, api ->
                    api.getDcModel().assertSingleOp("propose_finish_import_blockchain",
                            listOf(
                                    gtv("03ECD350EEBC617CBBFBEF0A1B7AE553A748021FD65C7C50C5ABB4CA16D4EA5B05".hexStringToByteArray()),
                                    gtv("BA3C3F7984398BF2FECD85DA4E3DAE752B22CE2977004D9E4FB10ED7DA5011CD".hexStringToByteArray()),
                                    gtv(100),
                                    gtv("Finish blockchain import from file - blockchain-rid: BA3C3F7984398BF2FECD85DA4E3DAE752B22CE2977004D9E4FB10ED7DA5011CD, final-height: 100")
                            ))
                }
    }

    @Test
    fun `fail if blockchain configuration are not loaded`(@TempDir dir: Path) {
        val config = writeResourceFileToTempDir(dir, "/city.configs")
        val configurationHeights = mapOf<Long, Gtv>(0L to GtvNull)
        buildTest(dir, configurationHeights)
                .testCommand(CommandProposeFinishBlockchainImport(),
                        "--configurations-file", config.absolutePath, "--final-height", "100"
                ) { result, api ->
                    assertThat(result.statusCode).isEqualTo(1)
                    assertThat(result.output.contains("Cannot finish blockchain import. Configurations for height(s): 33 have not been imported yet.")).isTrue()
                }
    }

    @Test
    fun `only check blockchain configurations up to --final-height`(@TempDir dir: Path) {
        val config = writeResourceFileToTempDir(dir, "/city.configs")
        val configurationHeights = mapOf<Long, Gtv>(0L to gtv(33L), 33L to GtvNull)
        buildTest(dir, configurationHeights)
                .testCommand(CommandProposeFinishBlockchainImport(),
                        "--configurations-file", config.absolutePath, "--final-height", "32"
                ) { result, api ->
                    api.getDcModel().assertSingleOp("propose_finish_import_blockchain",
                            listOf(
                                    gtv("03ECD350EEBC617CBBFBEF0A1B7AE553A748021FD65C7C50C5ABB4CA16D4EA5B05".hexStringToByteArray()),
                                    gtv("BA3C3F7984398BF2FECD85DA4E3DAE752B22CE2977004D9E4FB10ED7DA5011CD".hexStringToByteArray()),
                                    gtv(32),
                                    gtv("Finish blockchain import from file - blockchain-rid: BA3C3F7984398BF2FECD85DA4E3DAE752B22CE2977004D9E4FB10ED7DA5011CD, final-height: 32")
                            ))
                }
    }

    private fun buildTest(dir: Path, configurationHeights: Map<Long, Gtv>): ManagedRestTestApi {
        return ManagedRestTestApi(
                dir,
                dcVersion = 20L,
                pubKey = "03ECD350EEBC617CBBFBEF0A1B7AE553A748021FD65C7C50C5ABB4CA16D4EA5B05",
                privKey = "BBBDFE956021912512E14BB081B27A35A0EABC4098CB687E973C434006BCE114",
        )
                .withDCQuery("nm_find_next_configuration_height") { configurationHeights[it.args["height"]?.asInteger()]!! }
    }
}
