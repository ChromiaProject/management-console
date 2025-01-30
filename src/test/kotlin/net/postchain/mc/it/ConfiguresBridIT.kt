package net.postchain.mc.it

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.chromia.build.tools.TestProcess
import net.postchain.api.rest.controller.RestApi
import net.postchain.common.BlockchainRid
import net.postchain.common.PropertiesFileLoader
import net.postchain.mc.cli.economy.D1TestModel
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class ConfiguresBridIT {
    val dcBcRid = BlockchainRid.buildFromHex("ABABABABABABABABABABABABABABABABABABABABABABABABABABABABABABABAB")
    val ecBcRid = BlockchainRid.buildRepeat(1)

    @Test
    fun `missing brid gets auto-configured`(@TempDir dir: Path) {
        RestApi(0, "").use {
            val apiUrl = "http://localhost:${it.server.port()}"
            it.attachModel(dcBcRid, D1TestModel(dcBcRid, apiUrl, ecBcRid))
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=$apiUrl
            """.trimIndent())
            }
            TestProcess.Builder("network", "summary")
                    .awaitCompletion(true)
                    .setWorkingDir(dir.toFile())
                    .start {
                        val config = PropertiesFileLoader.load(dir.resolve(".chromia/config").absolutePathString())
                        assertThat(config.getString("brid")).isEqualTo(dcBcRid.toHex())
                    }
        }
    }
}