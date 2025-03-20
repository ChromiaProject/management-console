package net.postchain.mc.it

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.chromia.build.tools.TestProcess
import com.chromia.build.tools.restapi.TestModel
import net.postchain.api.rest.controller.Model
import net.postchain.api.rest.controller.RestApi
import net.postchain.chain0.common.queries.GetSummaryResult
import net.postchain.common.BlockchainRid
import net.postchain.common.PropertiesFileLoader
import net.postchain.common.exception.UserMistake
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtx.GtxQuery
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class ConfigurationIT {
    companion object {
        val dcBcRid = BlockchainRid.buildRepeat(1)
        const val PUBKEY = "03F7AB0AD49CC99773832549222E140603EF85B0904B78554BE5B87236712DF37E"
    }

    class D1TestModel(private val model: Model, private val apiUrl: String) : Model by model {
        constructor(blockchainRid: BlockchainRid, apiUrl: String) : this(TestModel(blockchainRid), apiUrl)

        override fun query(query: GtxQuery) = when (query.name) {
            "api_version" -> gtv(64)
            "cm_get_blockchain_api_urls" -> gtv(gtv(apiUrl))
            "get_relevant_proposals" -> {
                val myPubkey = query.args.asDict()["my_pubkey"]!!.asByteArray()
                if (myPubkey.contentEquals(PUBKEY.hexStringToByteArray())) {
                    gtv(listOf())
                } else {
                    throw UserMistake("Invalid pubkey: ${myPubkey.toHex()}")
                }
            }

            "get_provider_votes" -> gtv(listOf())
            "get_summary" -> GtvObjectMapper.toGtvDictionary(GetSummaryResult(12, 1, 2, 5, 10, 230))
            else -> throw IllegalArgumentException("Query not found: ${query.name}")
        }
    }

    @Test
    fun `missing brid gets auto-configured`(@TempDir dir: Path) {
        RestApi(0, "", gracefulShutdown = false).use {
            val apiUrl = "http://localhost:${it.server.port()}"
            it.attachModel(dcBcRid, D1TestModel(dcBcRid, apiUrl))
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=$apiUrl
            """.trimIndent())
            }
            TestProcess.Builder("network", "summary")
                    .awaitCompletion(true)
                    .setWorkingDir(dir.toFile())
                    .exitCode(0)
                    .wholeOutput("""
                    {
                      "Voter_sets": "5",
                      "Providers": "12",
                      "Clusters": "1",
                      "Containers": "2",
                      "Nodes": "10",
                      "Blockchains": "230"
                    }
                    """.trimIndent())
                    .start {
                        val config = PropertiesFileLoader.load(dir.resolve(".chromia/config").absolutePathString())
                        assertThat(config.getString("brid")).isEqualTo(dcBcRid.toHex())
                    }
        }
    }

    @Test
    fun `missing key`(@TempDir dir: Path) {
        RestApi(0, "", gracefulShutdown = false).use {
            val apiUrl = "http://localhost:${it.server.port()}"
            it.attachModel(dcBcRid, D1TestModel(dcBcRid, apiUrl))
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=$apiUrl
                brid=$dcBcRid
            """.trimIndent())
            }
            TestProcess.Builder("proposal", "list")
                    .awaitCompletion(true)
                    .setWorkingDir(dir.toFile())
                    .exitCode(1)
                    .wholeOutput("No keypair specified in configuration")
                    .start()
        }
    }

    @Test
    fun `keypair in config`(@TempDir dir: Path) {
        RestApi(0, "", gracefulShutdown = false).use {
            val apiUrl = "http://localhost:${it.server.port()}"
            it.attachModel(dcBcRid, D1TestModel(dcBcRid, apiUrl))
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=$apiUrl
                brid=$dcBcRid
                pubkey=$PUBKEY
                privkey=DC36585B89DD64D2F3A107FDA37C1730BEF3B3B13D5845B87C46EE235D0E9827
            """.trimIndent())
            }
            TestProcess.Builder("proposal", "list")
                    .awaitCompletion(true)
                    .setWorkingDir(dir.toFile())
                    .exitCode(0)
                    .wholeOutput("[]")
                    .start()
        }
    }

    @Test
    fun `keypair in secret file`(@TempDir dir: Path) {
        RestApi(0, "", gracefulShutdown = false).use {
            val apiUrl = "http://localhost:${it.server.port()}"
            it.attachModel(dcBcRid, D1TestModel(dcBcRid, apiUrl))
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=$apiUrl
                brid=$dcBcRid
            """.trimIndent())
            }
            with(File(dir.toFile(), "my_secret")) {
                writeText("""
                pubkey=$PUBKEY
                privkey=DC36585B89DD64D2F3A107FDA37C1730BEF3B3B13D5845B87C46EE235D0E9827
            """.trimIndent())
            }
            TestProcess.Builder("proposal", "list", "--secret", "my_secret")
                    .awaitCompletion(true)
                    .setWorkingDir(dir.toFile())
                    .exitCode(0)
                    .wholeOutput("[]")
                    .start()
        }
    }

    @Test
    fun `invalid secret file`(@TempDir dir: Path) {
        RestApi(0, "", gracefulShutdown = false).use {
            val apiUrl = "http://localhost:${it.server.port()}"
            it.attachModel(dcBcRid, D1TestModel(dcBcRid, apiUrl))
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=$apiUrl
                brid=$dcBcRid
            """.trimIndent())
            }
            with(File(dir.toFile(), "my_secret")) {
                writeText("""
                foo=bar    
            """.trimIndent())
            }
            TestProcess.Builder("proposal", "list", "--secret", "my_secret")
                    .awaitCompletion(true)
                    .setWorkingDir(dir.toFile())
                    .exitCode(1)
                    .wholeOutput("Secret file: my_secret does not contain 'pubkey' and/or 'privkey' properties")
                    .start()
        }
    }

    @Test
    fun `keypair in key id in config`(@TempDir dir: Path) {
        RestApi(0, "", gracefulShutdown = false).use {
            val apiUrl = "http://localhost:${it.server.port()}"
            it.attachModel(dcBcRid, D1TestModel(dcBcRid, apiUrl))
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=$apiUrl
                brid=$dcBcRid
                key.id=my_key                            
            """.trimIndent())
            }
            with(File(dir.toFile(), "my_key")) {
                writeText("DC36585B89DD64D2F3A107FDA37C1730BEF3B3B13D5845B87C46EE235D0E9827")
            }
            with(File(dir.toFile(), "my_key.pubkey")) {
                writeText(PUBKEY)
            }
            TestProcess.Builder("proposal", "list")
                    .env("CHROMIA_HOME" to dir.toFile().absolutePath)
                    .awaitCompletion(true)
                    .setWorkingDir(dir.toFile())
                    .exitCode(0)
                    .wholeOutput("[]")
                    .start()
        }
    }

    @Test
    fun `missing keypair in key id in config`(@TempDir dir: Path) {
        RestApi(0, "", gracefulShutdown = false).use {
            val apiUrl = "http://localhost:${it.server.port()}"
            it.attachModel(dcBcRid, D1TestModel(dcBcRid, apiUrl))
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=$apiUrl
                brid=$dcBcRid
                key.id=no_key                            
            """.trimIndent())
            }
            TestProcess.Builder("proposal", "list")
                    .env("CHROMIA_HOME" to dir.toFile().absolutePath)
                    .awaitCompletion(true)
                    .setWorkingDir(dir.toFile())
                    .exitCode(2)
                    .wholeOutput("Key with ID 'no_key' not found")
                    .start()
        }
    }

    @Test
    fun `keypair in key id on command line`(@TempDir dir: Path) {
        RestApi(0, "", gracefulShutdown = false).use {
            val apiUrl = "http://localhost:${it.server.port()}"
            it.attachModel(dcBcRid, D1TestModel(dcBcRid, apiUrl))
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=$apiUrl
                brid=$dcBcRid
            """.trimIndent())
            }
            with(File(dir.toFile(), "my_key")) {
                writeText("DC36585B89DD64D2F3A107FDA37C1730BEF3B3B13D5845B87C46EE235D0E9827")
            }
            with(File(dir.toFile(), "my_key.pubkey")) {
                writeText(PUBKEY)
            }
            TestProcess.Builder("proposal", "list", "--key-id", "my_key")
                    .env("CHROMIA_HOME" to dir.toFile().absolutePath)
                    .awaitCompletion(true)
                    .setWorkingDir(dir.toFile())
                    .exitCode(0)
                    .wholeOutput("[]")
                    .start()
        }
    }

    @Test
    fun `missing keypair in key id on command line`(@TempDir dir: Path) {
        RestApi(0, "", gracefulShutdown = false).use {
            val apiUrl = "http://localhost:${it.server.port()}"
            it.attachModel(dcBcRid, D1TestModel(dcBcRid, apiUrl))
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=$apiUrl
                brid=$dcBcRid
            """.trimIndent())
            }
            TestProcess.Builder("proposal", "list", "--key-id", "no_key")
                    .env("CHROMIA_HOME" to dir.toFile().absolutePath)
                    .awaitCompletion(true)
                    .setWorkingDir(dir.toFile())
                    .exitCode(1)
                    .wholeOutput("Key with ID 'no_key' not found")
                    .start()
        }
    }
}
