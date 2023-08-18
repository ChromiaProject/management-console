package net.postchain.mc.it

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import com.chromia.build.tools.RestApiInstance.apiUrl
import com.chromia.build.tools.RestApiInstance.withModel
import com.chromia.build.tools.TestModel
import com.chromia.build.tools.TestProcess
import net.postchain.api.rest.controller.Model
import net.postchain.chain0.common.queries.GetSummaryResult
import net.postchain.common.BlockchainRid
import net.postchain.common.PropertiesFileLoader
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtx.GtxQuery
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class AutoConfigurationIT {
    val testBrid = BlockchainRid.buildFromHex("ABABABABABABABABABABABABABABABABABABABABABABABABABABABABABABABAB")

    class SummaryModel(val model: Model) : Model by model {
        constructor(blockchainRid: BlockchainRid) : this(TestModel(blockchainRid))

        override fun query(query: GtxQuery) = when (query.name) {
            "cm_get_blockchain_api_urls" -> gtv(gtv("http://additionalhost:7740"))
            "get_summary" -> GtvObjectMapper.toGtvDictionary(GetSummaryResult(12, 1, 2, 5, 10, 230))
            else -> throw IllegalArgumentException("Query not found: ${query.name}")
        }
    }

    @Test
    fun `missing brid gets auto-configured`(@TempDir dir: Path) {
        withModel(SummaryModel(testBrid)) {
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
                        assertThat(config.getString("brid")).isEqualTo(testBrid.toHex())
                        assertThat(config.getList("api.url")).containsExactlyInAnyOrder(apiUrl, "http://additionalhost:7740")
                    }
        }
    }
}