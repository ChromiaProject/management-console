package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class SubnodeImageCommand : CliktCommand("Interacting with subnode images") {
    override fun run() = Unit
}

fun subnodeImageCommands() = SubnodeImageCommand().subcommands(
        CommandProposeSubnodeImage(),
        CommandProposeUpdateSubnodeImage(),
        CommandProposeDisableSubnodeImage(),
        CommandProposeEnableSubnodeImage(),
        CommandProposeAddSubnodeImageToCluster(),
        CommandProposeRemoveSubnodeImageFromCluster(),
        CommandGetSubnodeImageInfo(),
        CommandListSubnodeImages(),
)
