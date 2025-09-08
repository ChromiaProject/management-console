package net.postchain.mc.cli.blockchain

import assertk.assertThat
import assertk.assertions.exists
import assertk.assertions.isEqualTo
import net.postchain.common.BlockchainRid
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import net.postchain.gtv.GtvFactory.gtv
import kotlin.io.path.readText

class CommandGetBlockchainConfigurationIT {

    private val queryConfigResponse = gtv(GtvEncoder.encodeGtv(gtv(mapOf("a" to gtv(1), "b" to gtv(2)))))
    private val expectedConfigXML = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <dict>
                <entry key="a">
                    <int>1</int>
                </entry>
                <entry key="b">
                    <int>2</int>
                </entry>
            </dict>
        """.trimIndent()

    @Test
    fun `specific height - success`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("nm_get_blockchain_configuration", queryConfigResponse)
                .testCommand(CommandGetBlockchainConfiguration(),
                        "--blockchain-rid", BlockchainRid.buildRepeat(3).toHex(),
                        "--height", "100"
                ) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo(expectedConfigXML.trim())
                }
    }

    @Test
    fun `no height - success`(@TempDir dir: Path) {
        ManagedRestTestApi(dir)
                .withDCQuery("nm_get_blockchain_configuration", queryConfigResponse)
                .testCommand(CommandGetBlockchainConfiguration(),
                        "--blockchain-rid", BlockchainRid.buildRepeat(3).toHex()) { result, _ ->
                    assertThat(result.stdout.trim()).isEqualTo(expectedConfigXML.trim())
                }
    }

    @Test
    fun `save to file - success`(@TempDir dir: Path) {
        val outputFile = dir.resolve("output.xml")
        ManagedRestTestApi(dir)
                .withDCQuery("nm_get_blockchain_configuration", queryConfigResponse)
                .testCommand(CommandGetBlockchainConfiguration(),
                        "--blockchain-rid", BlockchainRid.buildRepeat(3).toHex(),
                        "--save", outputFile.toAbsolutePath().toString(),
                ) { _, _ ->
                    assertThat(outputFile).exists()
                    assertThat(outputFile.readText().trim()).isEqualTo(expectedConfigXML.trim())
                }
    }
}
