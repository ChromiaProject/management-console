package net.postchain.mc.cli.container

import com.github.ajalt.clikt.core.subcommands
import net.postchain.mc.cli.PmcCommand

class ContainerCommand : PmcCommand(help = "Container commands") {
    override fun run() = Unit
}

fun containerCommands() = ContainerCommand().subcommands(
        CommandProposeContainer(),
        CommandGetContainerInfo(),
        CommandProposeContainerResourceLimits(),
        CommandProposeContainerSubnodeImage(),
        CommandListContainers(),
        CommandProposeRemoveContainer(),
        CommandProposePauseContainer(),
        CommandResumeContainer(),
)