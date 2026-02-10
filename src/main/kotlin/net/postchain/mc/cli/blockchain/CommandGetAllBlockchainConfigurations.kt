package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.common.queries.getBlockchainInfo
import net.postchain.chain0.nm_api.nmFindNextConfigurationHeight
import net.postchain.chain0.nm_api.nmGetBlockchainConfiguration
import net.postchain.common.BlockchainRid
import net.postchain.gtv.GtvDecoder
import net.postchain.gtv.GtvEncoder
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.gtvml.GtvMLEncoder
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.blockchainOption
import net.postchain.mc.cli.resolveBlockchain
import net.postchain.mc.cli.util.pmcConfigOption
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class CommandGetAllBlockchainConfigurations : PmcCommand(
        name = "get-all-configurations",
        help = "List all blockchain configuration heights or download and save all configurations to the specified directory"
) {
    private val config by pmcConfigOption()

    private val blockchain by blockchainOption().required()

    private val save by option(help = "Where to save configuration file(s)").file(canBeFile = false, canBeDir = true)
    private val overwrite by option("--overwrite", help = "When saving configurations, overwrite existing files in the target directory").flag()
    private val exportFormat by option("--export-format", help = "When saving configurations, generate a binary GTV file for chain import instead of human readable XML files").flag()

    private val fromHeight by option("--from-height", help = "Fetch from height").long().default(0).validate {
        require(it >= 0) { "--from-height must be non-negative" }
    }
    private val toHeight by option("--to-height", help = "Fetch to height").long().default(Long.MAX_VALUE).validate {
        require(it >= 0) { "--to-height must be non-negative" }
    }

    override fun run() {
        val blockchainRID = resolveBlockchain(config.clientConfig, config.client, blockchain)
        if (config.client.getBlockchainInfo(blockchainRID.data) == null) {
            throw CliktError("Unknown blockchain: $blockchainRID")
        }

        if (toHeight < fromHeight) {
            throw CliktError("--to-height must be greater than or equal to --from-height")
        }

        val heights = findAllConfigurationHeights(blockchainRID)

        if (save != null) {
            downloadAndSaveConfigurations(blockchainRID, heights)
            if (heights.isEmpty()) {
                echo("No configurations downloaded")
            } else {
                echo("Configurations at heights downloaded:\n${heights.joinToString("\n")}")
            }
        } else {
            if (heights.isEmpty()) {
                echo("No configurations found")
            } else {
                echo("Configurations at heights found:\n${heights.joinToString("\n")}")
            }
        }
    }

    private fun findAllConfigurationHeights(blockchainRID: BlockchainRid): List<Long> {
        val heights = mutableListOf<Long>()

        var current = fromHeight - 1L
        while (true) {
            val next = config.client.nmFindNextConfigurationHeight(blockchainRID, current) ?: break
            when {
                next == toHeight -> {
                    heights.add(next)
                    break
                }

                next > toHeight -> {
                    break
                }

                else -> {
                    heights.add(next)
                    current = next
                }
            }
        }

        return heights
    }

    private fun downloadAndSaveConfigurations(blockchainRID: BlockchainRid, heights: List<Long>) {
        save?.mkdirs()
        if (!overwrite && save?.list()?.isNotEmpty() == true) {
            throw CliktError("Directory is not empty: $save")
        }

        if (exportFormat) {
            saveConfigurationsAsBinary(blockchainRID, heights)
        } else {
            saveConfigurationsAsXml(blockchainRID, heights)
        }
    }

    private fun saveConfigurationsAsBinary(blockchainRID: BlockchainRid, heights: List<Long>) {
        echo("Generating configuration export file with name: '$blockchainRID.configs'", err = true)
        BufferedOutputStream(FileOutputStream(File(save?.path, "$blockchainRID.configs"))).use { output ->
            output.write(GtvEncoder.encodeGtv(gtv(blockchainRID.data)))

            for (height in heights) {
                val bcConfig = config.client.nmGetBlockchainConfiguration(blockchainRID, height)
                        ?: throw CliktError("Blockchain configuration at height $height is absent")
                output.write(GtvEncoder.encodeGtv(gtv(gtv(height), gtv(bcConfig))))
            }

            output.write(GtvEncoder.encodeGtv(GtvNull))
        }
    }

    private fun saveConfigurationsAsXml(blockchainRID: BlockchainRid, heights: List<Long>) {
        for (height in heights) {
            val bcConfig = config.client.nmGetBlockchainConfiguration(blockchainRID, height)
                    ?: throw CliktError("Blockchain configuration at height $height is absent")
            val xmlGtv = GtvMLEncoder.encodeXMLGtv(GtvDecoder.decodeGtv(bcConfig))
            File(save?.path, "$height.conf.xml").writeText(xmlGtv)
        }
    }
}