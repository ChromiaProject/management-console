package net.postchain.mc.cli.image

import com.github.ajalt.clikt.core.subcommands
import net.postchain.mc.cli.PmcCommand

class SubnodeImageCommand : PmcCommand(help = "Interacting with subnode images") {
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
