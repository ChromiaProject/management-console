package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.nm_api.nmFindNextConfigurationHeight
import net.postchain.chain0.nm_api.nmGetBlockchainConfiguration
import net.postchain.gtv.GtvDecoder
import net.postchain.gtv.gtvml.GtvMLEncoder
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.clientOption
import java.io.File


class CommandGetAllBlockchainConfigurations : CliktCommand(
        name = "get-all-configurations",
        help = "Download all blockchain configurations  and save them to the specified directory."
) {
        private val client by clientOption()

        private val blockchainRID by blockchainRidOption().required()

        private val save by option(help = "where to save configurations XML").file(canBeFile = false, canBeDir = true).required()

        override fun run() {

                var height = 0L

                save.mkdirs()

                while (true) {
                        val bcConfig = client.nmGetBlockchainConfiguration(blockchainRID, height)

                        if (bcConfig == null) {
                                echo("Blockchain configuration at height $height is absent", err = true)
                                return
                        }

                        val xmlGtv = GtvMLEncoder.encodeXMLGtv(GtvDecoder.decodeGtv(bcConfig))
                        File(save.path + "/${height}.conf.xml").writeText(xmlGtv)

                        val nextHeight = client.nmFindNextConfigurationHeight(blockchainRID, height)
                        if (nextHeight != null)
                                height = nextHeight
                        else
                                break
                }
        }
}