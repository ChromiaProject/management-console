package net.postchain.mc.it

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.chromia.build.tools.TestProcess
import net.postchain.chain0.common.queries.GetSummaryResult
import net.postchain.common.BlockchainRid
import net.postchain.common.PropertiesFileLoader
import net.postchain.common.exception.UserMistake
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi.Companion.DEFAULT_PROVIDER_PUBKEY
import net.postchain.mc.cli.test_helpers.buildListProposalQueryResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class ConfigurationIT {

    @Test
    fun `missing brid gets auto-configured`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("get_summary", GtvObjectMapper.toGtvDictionary(GetSummaryResult(12, 1, 2, 5, 10, 230)))
                .afterServerBeforeTest { testApi ->
                    with(File(dir.toFile(), ".chromia/config")) {
                        parentFile.mkdirs()
                        writeText("""
                            api.url=${testApi.apiUrl}
                        """.trimIndent())
                    }
                }
                .test { testApi ->

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
                                assertThat(config.getString("brid")).isEqualTo(testApi.dcBcRid.toHex())
                            }
                }
    }

    @Test
    fun `missing key`(@TempDir dir: Path) {
        proposalTest(dir).test {
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=${it.apiUrl}
                brid=${it.dcBcRid}
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
        proposalTest(dir).test {
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
        proposalTest(dir).test {
            with(File(dir.toFile(), "my_secret")) {
                writeText("""
                pubkey=$DEFAULT_PROVIDER_PUBKEY
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
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=http://localhost:7740
                brid=${BlockchainRid.buildRepeat(1)}
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

    @Test
    fun `keypair in key id in config`(@TempDir dir: Path) {
        proposalTest(dir, keyId = "my_key").test {
            with(File(dir.toFile(), "my_key")) {
                writeText("DC36585B89DD64D2F3A107FDA37C1730BEF3B3B13D5845B87C46EE235D0E9827")
            }
            with(File(dir.toFile(), "my_key.pubkey")) {
                writeText(DEFAULT_PROVIDER_PUBKEY)
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
            with(File(dir.toFile(), ".chromia/config")) {
                parentFile.mkdirs()
                writeText("""
                api.url=http://localhost:7740
                brid=${BlockchainRid.buildRepeat(1)}
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

    @Test
    fun `keypair in key id on command line`(@TempDir dir: Path) {
        proposalTest(dir)
                .test {
                    with(File(dir.toFile(), "my_key")) {
                        writeText("DC36585B89DD64D2F3A107FDA37C1730BEF3B3B13D5845B87C46EE235D0E9827")
                    }
                    with(File(dir.toFile(), "my_key.pubkey")) {
                        writeText(DEFAULT_PROVIDER_PUBKEY)
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
        ManagedRestTestApi(dir).test {
            TestProcess.Builder("proposal", "list", "--key-id", "no_key")
                    .env("CHROMIA_HOME" to dir.toFile().absolutePath)
                    .awaitCompletion(true)
                    .setWorkingDir(dir.toFile())
                    .exitCode(1)
                    .wholeOutput("Key with ID 'no_key' not found")
                    .start()
        }
    }

    private fun proposalTest(dir: Path, keyId: String? = null): ManagedRestTestApi {
        return ManagedRestTestApi(dir, keyId = keyId)
                .withDCQuery("get_relevant_proposals",
                        buildListProposalQueryResponse(DEFAULT_PROVIDER_PUBKEY, listOf()))
                .withDCQuery("get_relevant_proposals") {
                    val myPubkey = it.args.asDict()["my_pubkey"]!!.asByteArray()
                    if (myPubkey.contentEquals(DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray())) {
                        gtv(listOf())
                    } else {
                        throw UserMistake("Invalid pubkey: ${myPubkey.toHex()}")
                    }
                }
                .withDCQuery("get_provider_votes", gtv(listOf()))
    }
}
