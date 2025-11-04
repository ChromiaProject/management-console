package net.postchain.mc.cli.jar_extension

import com.github.ajalt.clikt.core.subcommands
import net.postchain.mc.cli.PmcCommand

class SubnodeJarExtensionCommand : PmcCommand(help = "Interacting with subnode JAR extensions") {
    override fun run() = Unit
}

fun subnodeJarExtensionCommands() = SubnodeJarExtensionCommand().subcommands(
        CommandProposeSubnodeJarExtension(),
        CommandProposeUpdateSubnodeJarExtension(),
        CommandProposeDisableSubnodeJarExtension(),
        CommandProposeEnableSubnodeJarExtension(),
        CommandProposeAddSubnodeJarExtensionToCluster(),
        CommandProposeRemoveSubnodeJarExtensionFromCluster(),
        CommandGetSubnodeJarExtensionInfo(),
        CommandListSubnodeJarExtensions(),
        CommandDownloadSubnodeJarExtension()
)
