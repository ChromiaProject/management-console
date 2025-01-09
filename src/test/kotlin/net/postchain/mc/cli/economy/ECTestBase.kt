package net.postchain.mc.cli.economy

import assertk.assertThat
import assertk.assertions.isTrue
import net.postchain.api.rest.controller.RestApi
import net.postchain.common.BlockchainRid
import java.io.File
import java.nio.file.Path
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

open class ECTestBase {

    val testBrid = BlockchainRid.buildRepeat(0)
    val ecBcrid = BlockchainRid.buildRepeat(1)

    fun ecRestApiTest(dir: Path, ecVersion: Long, function: (ECTestModel) -> Unit) {

        RestApi(0, "").use { it ->
            val ecModel = ECTestModel(ecBcrid, ecVersion)
            it.attachModel(ecBcrid, ecModel)
            it.attachModel(testBrid, D1TestModel(testBrid, ecBcrid))
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=http://localhost:${it.server.port()}
            """.trimIndent())
            }

            function(ecModel)
        }
    }

    fun assertLineValue(lines: List<String>, name: String, value: String) {
        assertThat(lines
                .filter { it.contains("\"$name\":") }
                .any { it.contains(value) }
        ).isTrue()
    }
}