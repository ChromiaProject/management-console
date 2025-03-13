package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getSubnodeImage
import net.postchain.client.core.PostchainReadClient
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.network.requireApiVersion

class CommandGetSubnodeImageInfo : PmcCommand(
        name = "info",
        help = "Get information about a subnode image"
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOption("Subnode image name").required()

    override fun run() {
        client.requireApiVersion(57)
        showSubnodeImageInfo(client, name)
    }
}

fun CliktCommand.showSubnodeImageInfo(client: PostchainReadClient, name: String) {
    val info = client.getSubnodeImage(name)
    echo(pmcTable {
        body {
            row("Name:", info.name)
            row("URL:", info.url)
            row("Digest:", info.digest)
            row("Type:", info.subnodeImageType)
            row("Owner:", info.owner.toHex())
            row("Description:", info.description)
            row("GTX modules:", info.gtxModules)
            row("Sync extensions:", info.syncExts)
            row("Active:", info.active)
        }
    })
}
