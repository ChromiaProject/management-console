package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.SubnodeImageData
import net.postchain.chain0.common.queries.getSubnodeImage
import net.postchain.client.core.PostchainReadClient
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV91.getSubnodeImageV91

class CommandGetSubnodeImageInfo : DCBaseCommand(
        name = "info",
        help = "Get information about a subnode image",
        requiresVersion = 57
) {
    private val name by nameOption("Subnode image name").required()

    override fun runDC() {
        showSubnodeImageInfo(dcVersion, client, name)
    }
}

fun CliktCommand.showSubnodeImageInfo(dcVersion: Long, client: PostchainReadClient, name: String) {
    val info = if (dcVersion < 96) {
        val image = client.getSubnodeImageV91(name)
        SubnodeImageData(
                image.name,
                image.url,
                image.digest,
                image.subnodeImageType,
                image.owner,
                image.active,
                image.description,
                image.gtxModules,
                image.syncExts,
                image.baseComputeRequests,
                null
        )
    } else {
        client.getSubnodeImage(name)
    }
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
            row("Base compute requests:", info.baseComputeRequests)
            info.nativeFunctions?.let { row("Rell native functions:", it) }
        }
    })
}
