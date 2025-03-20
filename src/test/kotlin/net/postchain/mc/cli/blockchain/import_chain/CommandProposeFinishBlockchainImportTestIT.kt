package net.postchain.mc.cli.blockchain.import_chain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.chromia.build.tools.TestProcess
import net.postchain.api.rest.controller.RestApi
import net.postchain.common.BlockchainRid
import net.postchain.common.hexStringToByteArray
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class CommandProposeFinishBlockchainImportTestIT {
    val dcBcRid = BlockchainRid.buildRepeat(1)

    @Test
    fun `successful creating finish import blockchain proposal if all configurations are found`(@TempDir dir: Path) = RestApi(0, "", gracefulShutdown = false).use {
        val apiUrl = "http://localhost:${it.server.port()}"
        val dcModel = D1TestFinishImportModel(dcBcRid, apiUrl, mapOf<Long, Gtv>(0L to gtv(33L), 33L to GtvNull))
        it.attachModel(dcBcRid, dcModel)
        with(File(dir.toFile(), ".chromia/config")) {
            parentFile.mkdirs()
            writeText("""
                api.url = $apiUrl
                privkey = BBBDFE956021912512E14BB081B27A35A0EABC4098CB687E973C434006BCE114
                pubkey = 03ECD350EEBC617CBBFBEF0A1B7AE553A748021FD65C7C50C5ABB4CA16D4EA5B05
            """.trimIndent())
        }
        with(File(dir.toFile(), "city.configs")) {
            parentFile.mkdirs();
            {}.javaClass.getResourceAsStream("/city.configs")?.use { input ->
                outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        TestProcess.Builder("blockchain", "finish-import", "--configurations-file", "city.configs", "--final-height", "100")
                .awaitCompletion(true)
                .verbose()
                .setWorkingDir(dir.toFile())
                .start { _ ->
                    dcModel.assertSingleOp("propose_finish_import_blockchain",
                            listOf(
                                    gtv("03ECD350EEBC617CBBFBEF0A1B7AE553A748021FD65C7C50C5ABB4CA16D4EA5B05".hexStringToByteArray()),
                                    gtv("BA3C3F7984398BF2FECD85DA4E3DAE752B22CE2977004D9E4FB10ED7DA5011CD".hexStringToByteArray()),
                                    gtv(100),
                                    gtv("Finish blockchain import from file - blockchain-rid: BA3C3F7984398BF2FECD85DA4E3DAE752B22CE2977004D9E4FB10ED7DA5011CD, final-height: 100")
                            ))
                }
    }

    @Test
    fun `fail if blockchain configuration are not loaded`(@TempDir dir: Path) = RestApi(0, "", gracefulShutdown = false).use {
        val apiUrl = "http://localhost:${it.server.port()}"
        val dcModel = D1TestFinishImportModel(dcBcRid, apiUrl, mapOf<Long, Gtv>(0L to GtvNull))
        it.attachModel(dcBcRid, dcModel)
        with(File(dir.toFile(), ".chromia/config")) {
            parentFile.mkdirs()
            writeText("""
                api.url = $apiUrl
                privkey = BBBDFE956021912512E14BB081B27A35A0EABC4098CB687E973C434006BCE114
                pubkey = 03ECD350EEBC617CBBFBEF0A1B7AE553A748021FD65C7C50C5ABB4CA16D4EA5B05
            """.trimIndent())
        }
        with(File(dir.toFile(), "city.configs")) {
            parentFile.mkdirs();
            {}.javaClass.getResourceAsStream("/city.configs")?.use { input ->
                outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        TestProcess.Builder("blockchain", "finish-import", "--configurations-file", "city.configs", "--final-height", "100")
                .awaitCompletion(true)
                .setWorkingDir(dir.toFile())
                .exitCode(1)
                .start { testProcess ->
                    assertThat(testProcess.process.exitValue()).isEqualTo(1)
                    assertThat(testProcess.readLines().contains("Cannot finish blockchain import. Configurations for height(s): 33 have not been imported yet.")).isTrue()
                }
    }

    @Test
    fun `only check blockchain configurations up to --final-height`(@TempDir dir: Path) = RestApi(0, "").use { it ->
        val apiUrl = "http://localhost:${it.server.port()}"
        val dcModel = D1TestFinishImportModel(dcBcRid, apiUrl, mapOf<Long, Gtv>(0L to gtv(33L), 33L to GtvNull))
        it.attachModel(dcBcRid, dcModel)
        with(File(dir.toFile(), ".chromia/config")) {
            parentFile.mkdirs()
            writeText("""
                api.url = $apiUrl
                privkey = BBBDFE956021912512E14BB081B27A35A0EABC4098CB687E973C434006BCE114
                pubkey = 03ECD350EEBC617CBBFBEF0A1B7AE553A748021FD65C7C50C5ABB4CA16D4EA5B05
            """.trimIndent())
        }
        with(File(dir.toFile(), "city.configs")) {
            parentFile.mkdirs();
            {}.javaClass.getResourceAsStream("/city.configs")?.use { input ->
                outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        TestProcess.Builder("blockchain", "finish-import", "--configurations-file", "city.configs", "--final-height", "32")
                .awaitCompletion(true)
                .setWorkingDir(dir.toFile())
                .start { _ ->
                    dcModel.assertSingleOp("propose_finish_import_blockchain",
                            listOf(
                                    gtv("03ECD350EEBC617CBBFBEF0A1B7AE553A748021FD65C7C50C5ABB4CA16D4EA5B05".hexStringToByteArray()),
                                    gtv("BA3C3F7984398BF2FECD85DA4E3DAE752B22CE2977004D9E4FB10ED7DA5011CD".hexStringToByteArray()),
                                    gtv(32),
                                    gtv("Finish blockchain import from file - blockchain-rid: BA3C3F7984398BF2FECD85DA4E3DAE752B22CE2977004D9E4FB10ED7DA5011CD, final-height: 32")
                            ))
                }
    }

}