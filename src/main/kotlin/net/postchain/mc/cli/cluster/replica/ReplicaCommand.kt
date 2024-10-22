package net.postchain.mc.cli.cluster.replica

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands

class ReplicaCommand : NoOpCliktCommand() {
    override fun help(context: Context) = "Cluster replica commands"
}

fun clusterReplicaCommands() = ReplicaCommand().subcommands(
        CommandAddClusterReplica(),
        CommandRemoveClusterReplica()
)