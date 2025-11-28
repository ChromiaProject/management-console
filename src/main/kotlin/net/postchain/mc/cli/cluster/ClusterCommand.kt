package net.postchain.mc.cli.cluster

import com.github.ajalt.clikt.core.subcommands
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.cluster.replica.clusterReplicaCommands

class ClusterCommand : PmcCommand(help = "Interacting with clusters") {
    override fun run() = Unit
}

fun clusterCommands() = ClusterCommand().subcommands(
        CommandListClusters(),
        CommandAddCluster(),
        CommandGetClusterInfo(),
        CommandListClusterContainers(),
        CommandProposeClusterProvider(),
        CommandProposeClusterResourceLimits(),
        CommandProposeRemoveCluster(),
        CommandClusterVerify(),
        clusterReplicaCommands()
)