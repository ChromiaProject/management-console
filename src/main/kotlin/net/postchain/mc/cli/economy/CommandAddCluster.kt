package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.createClusterOperation
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.ECONOMY_CHAIN_MAX_CLUSTER_NODES_VERSION
import net.postchain.mc.cli.base.ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.clusterUnitsOption
import net.postchain.mc.cli.util.containerUnitCpuOption
import net.postchain.mc.cli.util.containerUnitIoReadOption
import net.postchain.mc.cli.util.containerUnitIoWriteOption
import net.postchain.mc.cli.util.containerUnitRamOption
import net.postchain.mc.cli.util.containerUnitStorageOption
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.extraStorageOption
import net.postchain.mc.cli.util.maxNodes
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.systemContainerUnitsOption
import net.postchain.mc.compatibility.ApiCompatECV56.createClusterOperationV56
import net.postchain.mc.compatibility.ApiCompatECV62.createClusterOperationV62

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
    private val systemContainerUnits by systemContainerUnitsOption()
    private val containerUnitCpu by containerUnitCpuOption()
    private val containerUnitRam by containerUnitRamOption()
    private val containerUnitStorage by containerUnitStorageOption()
    private val containerUnitIoRead by containerUnitIoReadOption()
    private val containerUnitIoWrite by containerUnitIoWriteOption()

    private val maxNodes by maxNodes()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        when {
            ecVersion.version >= ECONOMY_CHAIN_MAX_CLUSTER_NODES_VERSION -> {
                transactionBuilder().createClusterOperation(
                        clientProviderPubkey, name, governorName, voterSet, clusterUnits, extraStorage, tag,
                        containerUnitCpu, containerUnitRam, containerUnitIoRead, containerUnitIoWrite,
                        containerUnitStorage, systemContainerUnits, maxNodes)
            }

            ecVersion.version >= ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION -> {
                transactionBuilder().createClusterOperationV62(
                        clientProviderPubkey, name, governorName, voterSet, clusterUnits, extraStorage, tag,
                        containerUnitCpu, containerUnitRam, containerUnitIoRead, containerUnitIoWrite,
                        containerUnitStorage, systemContainerUnits)
            }

            else -> {
                transactionBuilder().createClusterOperationV56(name, governorName, voterSet, clusterUnits, extraStorage, tag)
            }
        }
                .postOrSave()
                .printResult(
                        "Proposal for creating cluster $name is created and awaits approval. You can check economy proposal or $CLUSTER_CREATION_STATUS_COMMAND command to see the status",
                        "Failed to create cluster proposal"
                )
    }
}