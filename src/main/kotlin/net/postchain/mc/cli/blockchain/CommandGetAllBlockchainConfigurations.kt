package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.common.queries.getBlockchainInfo
import net.postchain.chain0.nm_api.nmFindNextConfigurationHeight
import net.postchain.chain0.nm_api.nmGetBlockchainConfiguration
import net.postchain.gtv.GtvDecoder
import net.postchain.gtv.GtvEncoder
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.gtvml.GtvMLEncoder
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pmcConfigOption
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class CommandGetAllBlockchainConfigurations : PmcCommand(
        name = "get-all-configurations",
        help = "List all blockchain configuration heights or download and save all configurations to the specified directory"
) {
    private val config by pmcConfigOption()

    private val blockchainRID by blockchainRidOption().required()

    private val save by option(help = "Where to save configuration file(s)").file(canBeFile = false, canBeDir = true)

    private val overwrite by option("--overwrite", help = "Overwrite existing files").flag()

    private val exportFormat by option("--export-format", help = "Will generate a binary GTV file that can be used for chain import instead of human readable XML files").flag()

    private val fromHeight by option("--from-height", help = "Fetch from height").long().default(0)

    private val toHeight by option("--to-height", help = "Fetch to height").long()

    override fun run() {
        if (config.client.getBlockchainInfo(blockchainRID.data) == null) {
            throw CliktError("Unknown blockchain: $blockchainRID")
        }

        val heights = if (fromHeight == 0L) {
            mutableListOf(0L)
        } else {
            var startHeight = 0L
            while (startHeight != fromHeight) {
                val nextHeight = config.client.nmFindNextConfigurationHeight(blockchainRID, startHeight)
                if (nextHeight == null || nextHeight > fromHeight) break
                startHeight = nextHeight
            }
            mutableListOf(startHeight)
        }

        var exportConfigFile: OutputStream? = null
        if (save != null) {
            save?.mkdirs()
            if (!overwrite && save?.list()?.isNotEmpty() == true) {
                throw CliktError("Directory is not empty: $save")
            }
            if (exportFormat) {
                echo("Generating configuration export file with name: '$blockchainRID.configs'", err = true)
                exportConfigFile = BufferedOutputStream(FileOutputStream(File(save?.path, "$blockchainRID.configs")))
            }
        }

        val stopHeight = toHeight
        try {
            exportConfigFile?.write(GtvEncoder.encodeGtv(gtv(blockchainRID.data)))
            while (true) {
                if (save != null) {
                    val height = heights.last()
                    val bcConfig = config.client.nmGetBlockchainConfiguration(blockchainRID, height)
                    if (bcConfig == null) {
                        echo("Blockchain configuration at height $height is absent", err = true)
                        heights.removeLast()
                        return
                    }

                    if (exportConfigFile != null) {
                        exportConfigFile.write(GtvEncoder.encodeGtv(gtv(gtv(height), gtv(bcConfig))))
                    } else {
                        val xmlGtv = GtvMLEncoder.encodeXMLGtv(GtvDecoder.decodeGtv(bcConfig))
                        File(save?.path, "${heights.last()}.conf.xml").writeText(xmlGtv)
                    }
                }

                val nextHeight = config.client.nmFindNextConfigurationHeight(blockchainRID, heights.last()) ?: break
                if (stopHeight != null && nextHeight > stopHeight) break
                heights.add(nextHeight)
            }
            exportConfigFile?.write(GtvEncoder.encodeGtv(GtvNull))
        } finally {
            exportConfigFile?.close()
        }

        if (heights.isEmpty()) {
            echo("No configurations ${if (save != null) "downloaded" else "found"}")
        } else {
            echo("Configurations at heights ${if (save != null) "downloaded" else "found"}:\n${heights.joinToString("\n")}")
        }
    }
}