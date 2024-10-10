package net.postchain.mc.cli.image

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getSubnodeImages
import net.postchain.mc.cli.base.NAME_LENGTH_MAX
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.network.requireApiVersion

class CommandListSubnodeImages : PmcCommand(
        name = "list",
        help = "List all subnode images"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()

    private val headers = listOf("Name", "URL", "Digest", "Type", "Owner", "Description", "Active")

    override fun run() {
        client.requireApiVersion(57)
        val images = client.getSubnodeImages()
        echo(pmcTable(
                "images",
                headers,
                images.map { listOf(it.name, it.url, it.digest, it.subnodeImageType.name, it.owner.toHex(), it.description, it.active.toString()) },
                0 to NAME_LENGTH_MAX,
                interactive
        ))
        if (interactive && images.isNotEmpty()) {
            promptForIndex(images)?.let {
                showSubnodeImageInfo(client, images[it].name)
            }
        }
    }
}
