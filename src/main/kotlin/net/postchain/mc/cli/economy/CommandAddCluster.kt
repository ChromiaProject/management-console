package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.economy.economy_chain.createClusterOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.clusterUnitsOption
import net.postchain.mc.cli.util.extraStorageOption
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption

class CommandAddCluster : CliktCommand(
        name = "add-cluster",
        help = "Add a new cluster"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOption("Name of the new cluster").required()

    private val voterSet by option("-vs", "--voter-set", help = "Cluster voter set").required()

    private val clusterUnits by clusterUnitsOption().default(1)

    private val extraStorage by extraStorageOption().default(0)

    private val governorName by option(
            "-g", "--governor",
            help = "Name of another voter set which can update this cluster."
    ).required()

    private val tag by option("-t", "--tag", help = "Cluster tag").required()

    override fun run() {
        val economyChainClient = getEconomyChainClient(client, config.config)

        economyChainClient.transactionBuilder()
                .createClusterOperation(name, governorName, voterSet, clusterUnits, extraStorage, tag)
                .postAwaitConfirmation()
                .printResult(
                        "Cluster $name is in pending creation. You can check get-cluster-creation-status command to see the status.",
                        "Failed to create cluster"
                )
    }
}