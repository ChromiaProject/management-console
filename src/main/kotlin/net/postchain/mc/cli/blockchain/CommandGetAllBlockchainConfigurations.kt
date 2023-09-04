package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.common.queries.getBlockchains
import net.postchain.chain0.nm_api.nmFindNextConfigurationHeight
import net.postchain.chain0.nm_api.nmGetBlockchainConfiguration
import net.postchain.gtv.GtvDecoder
import net.postchain.gtv.gtvml.GtvMLEncoder
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pmcConfigOption
import java.io.File


class CommandGetAllBlockchainConfigurations : CliktCommand(
        name = "get-all-configurations",
        help = "List all blockchain configuration heights or download and save all configurations to the specified directory"
) {
    private val config by pmcConfigOption()

    private val blockchainRID by blockchainRidOption().required()

    private val save by option(help = "Where to save configurations XML").file(canBeFile = false, canBeDir = true)

    private val overwrite by option("--overwrite", help = "Overwrite existing files").flag()

    override fun run() {
        if (config.client.getBlockchains(true).none { it.rid == blockchainRID.wData }) {
            echo("Unknown blockchain: $blockchainRID")
            return
        }

        val heights = mutableListOf(0L)

        if (save != null) {
            save?.mkdirs()
            if (!overwrite && save?.list()?.isNotEmpty() == true) {
                echo("Directory is not empty: $save", err = true)
                return
            }
        }

        while (true) {
            if (save != null) {
                val bcConfig = config.client.nmGetBlockchainConfiguration(blockchainRID, heights.last())
                if (bcConfig == null) {
                    echo("Blockchain configuration at height ${heights.last()} is absent", err = true)
                    heights.removeLast()
                    return
                }

                val xmlGtv = GtvMLEncoder.encodeXMLGtv(GtvDecoder.decodeGtv(bcConfig))
                File(save?.path, "${heights.last()}.conf.xml").writeText(xmlGtv)
            }

            val nextHeight = config.client.nmFindNextConfigurationHeight(blockchainRID, heights.last()) ?: break
            heights.add(nextHeight)
        }

        if (heights.isEmpty()) {
            echo("No configurations ${if (save != null) "downloaded" else "found"}")
        } else {
            echo("Configurations at heights ${if (save != null) "downloaded" else "found"}: ${heights.joinToString(", ")}")
        }
    }
}