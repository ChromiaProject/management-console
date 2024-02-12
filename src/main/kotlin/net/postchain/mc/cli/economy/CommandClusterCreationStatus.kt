package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.ClusterCreationStatus
import net.postchain.economy.economy_chain.getClusterCreationStatus
import net.postchain.mc.cli.util.nameOption

const val CLUSTER_CREATION_STATUS_COMMAND = "cluster-creation-status"

class CommandClusterCreationStatus : ECBaseCommand(
        name = CLUSTER_CREATION_STATUS_COMMAND,
        help = "Get cluster creation status"
)  {
    private val name by nameOption("Name of the cluster to get creation status").required()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        val status = economyChainClient.getClusterCreationStatus(name)
        when(status){
            ClusterCreationStatus.SUCCESS -> echo("Cluster with name $name is created successfully.")
            ClusterCreationStatus.UNKNOWN -> echo("Cluster with name $name is unknown.")
            ClusterCreationStatus.PENDING_APPROVAL -> echo("Cluster with name $name is pending approval.")
            ClusterCreationStatus.PENDING_CREATION -> echo("Cluster with name $name is pending creation.")
            ClusterCreationStatus.FAILURE -> echo("Cluster with name $name failed to be created.")
        }
    }
}