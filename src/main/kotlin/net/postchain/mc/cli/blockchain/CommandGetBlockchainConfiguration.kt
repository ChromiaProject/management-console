package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.nm_api.nmGetBlockchainConfiguration
import net.postchain.gtv.GtvDecoder
import net.postchain.gtv.gtvml.GtvMLEncoder
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.heightOption
import net.postchain.mc.cli.util.pmcConfigOption

open class CommandGetBlockchainConfiguration(
        name: String = "get-configuration",
        help: String = "Get blockchain configuration"
) : CliktCommand(
        name = name,
        help = help
) {
    private val config by pmcConfigOption()

    private val blockchainRID by blockchainRidOption().required()

    private val height by heightOption().default(Long.MAX_VALUE)

    private val save by option(help = "where to save configuration XML").file(canBeFile = true, canBeDir = false)

    override fun run() {
        val message = if (height == Long.MAX_VALUE) "Last blockchain configuration" else "Blockchain configuration at height $height"
        val bcConfig = config.client.nmGetBlockchainConfiguration(blockchainRID, height)
        if (bcConfig == null) {
            echo("$message is absent")
        } else {
            val xmlGtv = GtvMLEncoder.encodeXMLGtv(GtvDecoder.decodeGtv(bcConfig))
            if (save != null) {
                save!!.parentFile?.mkdirs()
                save!!.writeText(xmlGtv)
                echo("$message saved to ${save!!.name}")
            } else {
                echo("$message:")
                echo(xmlGtv)
            }
        }
    }
}
