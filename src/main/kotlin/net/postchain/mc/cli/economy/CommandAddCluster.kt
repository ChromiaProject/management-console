package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.createClusterOperation
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.clusterUnitsOption
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.extraStorageOption
import net.postchain.mc.cli.util.nameOption

class CommandAddCluster : ECBaseCommand(
        name = "add-cluster",
        help = "Add a new cluster"
) {
    private val name by nameOption("Name of the new cluster").required().validate(entityNameValidator())

    private val voterSet by option("-vs", "--voter-set", help = "Cluster voter set").required()

    private val clusterUnits by clusterUnitsOption().default(1)

    private val extraStorage by extraStorageOption().default(0)

    private val governorName by option(
            "-g", "--governor",
            help = "Name of another voter set which can update this cluster."
    ).required()

    private val tag by option("-t", "--tag", help = "Cluster tag").required()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        economyChainClient.transactionBuilder()
                .createClusterOperation(name, governorName, voterSet, clusterUnits, extraStorage, tag)
                .postAwaitConfirmation()
                .printResult(
                        "Proposal for creating cluster $name is created and awaits approval. You can check economy proposal or $CLUSTER_CREATION_STATUS_COMMAND command to see the status",
                        "Failed to create cluster proposal"
                )
    }
}