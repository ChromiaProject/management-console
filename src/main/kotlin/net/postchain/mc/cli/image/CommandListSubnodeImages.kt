package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.input.interactiveSelectList
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.NAME_LENGTH_MAX
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV91.getSubnodeImagesV91

class CommandListSubnodeImages : DCBaseCommand(
        name = "list",
        help = "List all subnode images",
        requiresVersion = 57,
        printHelpOnEmptyArgs = false
) {
    private val interactive by interactiveOption()

    private val headers = listOf("Name", "URL", "Digest", "Type", "Owner", "Description", "Active")

    override fun runDC() {
        val images = client.getSubnodeImagesV91()
        if (interactive && images.isNotEmpty() && images.size < terminal.size.height) {
            terminal.interactiveSelectList(images.map {
                "${it.name} - ${it.url}"
            }, "Select subnode image")?.let {
                showSubnodeImageInfo(dcVersion, client, it.split(' ').first())
            }
        } else {
            echo(pmcTable(
                    "images",
                    headers,
                    images.map { listOf(it.name, it.url, it.digest, it.subnodeImageType.name, it.owner.toHex(), it.description, it.active.toString()) },
                    0 to NAME_LENGTH_MAX,
                    interactive
            ))
            if (interactive && images.isNotEmpty()) {
                promptForIndex(images)?.let {
                    showSubnodeImageInfo(dcVersion, client, images[it].name)
                }
            }
        }
    }
}
