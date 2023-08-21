package net.postchain.mc.it

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.chromia.build.tools.TestModel
import com.chromia.build.tools.TestProcess
import net.postchain.api.rest.controller.Model
import net.postchain.api.rest.controller.RestApi
import net.postchain.chain0.common.queries.GetSummaryResult
import net.postchain.common.BlockchainRid
import net.postchain.common.PropertiesFileLoader
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtx.GtxQuery
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class ConfiguresBridIT {
    val testBrid = BlockchainRid.buildFromHex("ABABABABABABABABABABABABABABABABABABABABABABABABABABABABABABABAB")

    class SummaryModel(val model: Model) : Model by model {
        constructor(blockchainRid: BlockchainRid) : this(TestModel(blockchainRid))

        override fun query(query: GtxQuery) = when (query.name) {
            "get_summary" -> GtvObjectMapper.toGtvDictionary(GetSummaryResult(12, 1, 2, 5, 10, 230))
            else -> throw IllegalArgumentException("Query not found: ${query.name}")
        }
    }

    @Test
    fun `missing brid gets auto-configured`(@TempDir dir: Path) {
        RestApi(7740, "").use {
            it.attachModel(testBrid, SummaryModel(testBrid))
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=http://localhost:7740
            """.trimIndent())
            }
            TestProcess.Builder("network", "summary")
                    .awaitCompletion(true)
                    .setWorkingDir(dir.toFile())
                    .start {
                        val config = PropertiesFileLoader.load(dir.resolve(".chromia/config").absolutePathString())
                        assertThat(config.getString("brid")).isEqualTo(testBrid.toHex())
                    }
        }
    }
}