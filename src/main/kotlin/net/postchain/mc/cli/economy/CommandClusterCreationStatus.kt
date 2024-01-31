package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.economy.economy_chain.ClusterCreationStatus
import net.postchain.economy.economy_chain.getClusterCreationStatus
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption

class CommandClusterCreationStatus : CliktCommand(
        name = "cluster-creation-status",
        help = "Get cluster creation status"
)  {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOption("Name of the cluster to get creation status").required()

    override fun run() {
        val economyChainClient = getEconomyChainClient(client, config.config)
        val status = economyChainClient.getClusterCreationStatus(name)
        when(status){
            ClusterCreationStatus.SUCCESS -> echo("Cluster with name $name is created successfully.")
            ClusterCreationStatus.UNKNOWN -> echo("Cluster with name $name is unknown.")
            ClusterCreationStatus.PENDING -> echo("Cluster with name $name is pending creation.")
            ClusterCreationStatus.FAILURE -> echo("Cluster with name $name failed to be created.")
        }
    }
}