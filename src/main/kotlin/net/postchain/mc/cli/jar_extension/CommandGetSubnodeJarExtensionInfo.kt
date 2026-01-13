package net.postchain.mc.cli.jar_extension

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getSubnodeJarExtension
import net.postchain.client.core.PostchainClient
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcTable

class CommandGetSubnodeJarExtensionInfo : DCBaseCommand(
        name = "info",
        help = "Get information about a subnode JAR extension. To download the JAR, use the 'pmc subnode-jar-extension download-jar' command.",
        requiresVersion = 102
) {
    private val name by nameOption("Subnode JAR extension name").required()

    override fun runDC() {
        showSubnodeJarExtensionInfo(client, name)
    }
}

fun CliktCommand.showSubnodeJarExtensionInfo(client: PostchainClient, name: String) {
    val info = client.getSubnodeJarExtension(name)
    echo(pmcTable {
        body {
            row("Name:", info.name)
            row("Type:", info.subnodeJarExtensionType)
            row("Owner:", info.owner.toHex())
            row("Description:", info.description)
            row("GTX modules:", info.gtxModules)
            row("Sync extensions:", info.syncExts)
            row("Active:", info.active)
            info.nativeFunctions?.let { row("Rell native functions:", it) }
        }
    })

}
