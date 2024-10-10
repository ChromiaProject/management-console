package net.postchain.mc.cli.anchoring

import com.github.ajalt.clikt.core.subcommands
import net.postchain.mc.cli.PmcCommand

class ClusterAnchoringCommand : PmcCommand(help = "Interactions with cluster anchoring chain configuration") {
    override fun run() = Unit
}

fun clusterAnchoringCommands() = ClusterAnchoringCommand().subcommands(
    CommandProposeClusterAnchoringConfiguration(),
    CommandGetClusterAnchoringConfiguration()
)
