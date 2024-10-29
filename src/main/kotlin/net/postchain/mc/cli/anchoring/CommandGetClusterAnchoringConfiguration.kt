package net.postchain.mc.cli.anchoring

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.common.queries.getClusterAnchoringConfiguration
import net.postchain.gtv.GtvDecoder
import net.postchain.gtv.gtvml.GtvMLEncoder
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.util.pmcConfigOption

class CommandGetClusterAnchoringConfiguration : PmcCommand(
        name = "get",
        help = "Get cluster anchoring configuration"
) {
    private val config by pmcConfigOption()

    private val save by option(help = "where to save configuration, format will be determined by file extension: .xml or .gtv").file(canBeFile = true, canBeDir = false)

    override fun run() {
        val ac = config.client.getClusterAnchoringConfiguration()
        if (save != null) {
            save!!.parentFile?.mkdirs()
            when (save!!.extension) {
                "gtv" -> save!!.writeBytes(ac)

                else -> save!!.writer().use {
                    GtvMLEncoder.encodeXML(GtvDecoder.decodeGtv(ac), it, strict = false)
                }
            }
        } else {
            echo(GtvMLEncoder.encodeXMLGtv(GtvDecoder.decodeGtv(ac)))
        }
    }
}
