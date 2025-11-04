package net.postchain.mc.cli.jar_extension

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.input.interactiveSelectList
import net.postchain.chain0.common.queries.getSubnodeJarExtensions
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.NAME_LENGTH_MAX
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcTable

class CommandListSubnodeJarExtensions : DCBaseCommand(
        name = "list",
        help = "List all subnode JAR extensions",
        requiresVersion = 102,
        printHelpOnEmptyArgs = false
) {
    private val interactive by interactiveOption()

    private val headers = listOf("Name", "Type", "Owner", "Description", "Active")

    override fun runDC() {
        val extensions = client.getSubnodeJarExtensions()
        if (interactive && extensions.isNotEmpty() && extensions.size < terminal.size.height) {
            terminal.interactiveSelectList(extensions.map { it.name }, "Select subnode JAR extension")?.let {
                showSubnodeJarExtensionInfo(client, it.split(' ').first())
            }
        } else {
            echo(pmcTable(
                    "extensions",
                    headers,
                    extensions.map { listOf(it.name, it.subnodeJarExtensionType.name, it.owner.toHex(), it.description, it.active.toString()) },
                    0 to NAME_LENGTH_MAX,
                    interactive
            ))
            if (interactive && extensions.isNotEmpty()) {
                promptForIndex(extensions)?.let {
                    showSubnodeJarExtensionInfo(client, extensions[it].name)
                }
            }
        }
    }
}
