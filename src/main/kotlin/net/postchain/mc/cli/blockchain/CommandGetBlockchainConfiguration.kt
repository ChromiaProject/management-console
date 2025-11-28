package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.nm_api.nmGetBlockchainConfiguration
import net.postchain.gtv.GtvDecoder
import net.postchain.gtv.gtvml.GtvMLEncoder
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.blockchainOption
import net.postchain.mc.cli.heightOption
import net.postchain.mc.cli.resolveBlockchain
import net.postchain.mc.cli.util.pmcConfigOption

open class CommandGetBlockchainConfiguration(
        name: String = "get-configuration",
        help: String = "Get blockchain configuration"
) : PmcCommand(
        name = name,
        help = help
) {
    private val config by pmcConfigOption()

    private val blockchain by blockchainOption().required()

    private val height by heightOption().default(Long.MAX_VALUE)

    private val save by option(help = "where to save configuration, format will be determined by file extension: .xml or .gtv").file(canBeFile = true, canBeDir = false)

    override fun run() {
        val message = if (height == Long.MAX_VALUE) "Last blockchain configuration" else "Blockchain configuration at height $height"
        val blockchainRID = resolveBlockchain(config.clientConfig, config.client, blockchain)
        val bcConfig = config.client.nmGetBlockchainConfiguration(blockchainRID, height)
        if (bcConfig == null) {
            echo("$message is absent", err = true)
        } else {
            if (save != null) {
                save!!.parentFile?.mkdirs()
                when (save!!.extension) {
                    "gtv" -> save!!.writeBytes(bcConfig)

                    else -> save!!.writer().use {
                        GtvMLEncoder.encodeXML(GtvDecoder.decodeGtv(bcConfig), it, strict = false)
                    }
                }
                echo("$message saved to ${save!!.name}", err = true)
            } else {
                echo("$message:", err = true)
                echo(GtvMLEncoder.encodeXMLGtv(GtvDecoder.decodeGtv(bcConfig)))
            }
        }
    }
}
