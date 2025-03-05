package net.postchain.mc.cli.economy

import assertk.assertThat
import assertk.assertions.isTrue
import net.postchain.api.rest.controller.RestApi
import net.postchain.common.BlockchainRid
import java.io.File
import java.nio.file.Path

open class ECTestBase {
    val dcBcRid = BlockchainRid.buildRepeat(0)
    val ecBcRid = BlockchainRid.buildRepeat(1)

    fun ecRestApiTest(dir: Path, ecVersion: Long, function: (ECTestModel) -> Unit) {

        RestApi(0, "").use {
            val apiUrl = "http://localhost:${it.server.port()}"
            val ecModel = ECTestModel(ecBcRid, ecVersion)
            it.attachModel(ecBcRid, ecModel)
            it.attachModel(dcBcRid, D1TestModel(dcBcRid, apiUrl, ecBcRid))
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=$apiUrl
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