package net.postchain.mc.cli.lease

import com.github.ajalt.clikt.core.subcommands
import net.postchain.mc.cli.PmcCommand

class LeaseCommand : PmcCommand(help = "Lease commands") {
    override fun run() = Unit
}

fun leaseCommands() = LeaseCommand().subcommands(
        CommandListLeases(),
        CommandGetLeaseInfo(),
        CommandUpgradeContainer(),
        CommandAssignSubnodeImageToContainer(),
        CommandCreateContainer(),
        CommandRemoveContainer(),
        CommandListPendingLeaseTickets(),
        CommandAddSubnodeJarExtensionsToContainer()
)
