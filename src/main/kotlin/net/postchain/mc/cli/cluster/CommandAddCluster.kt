package net.postchain.mc.cli.cluster

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.direct_cluster.createClusterFromOperation
import net.postchain.chain0.direct_cluster.createClusterFromWithClusterDataOperation
import net.postchain.chain0.direct_cluster.createClusterFromWithUnitsOperation
import net.postchain.chain0.direct_cluster.createClusterOperation
import net.postchain.chain0.direct_cluster.createClusterWithClusterDataOperation
import net.postchain.chain0.direct_cluster.createClusterWithUnitsOperation
import net.postchain.chain0.features.hasDirectCluster
import net.postchain.chain0.model.ClusterCreationData
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.VoterSetOrPubkeysOption
import net.postchain.mc.cli.util.clusterUnitsOption
import net.postchain.mc.cli.util.containerUnitCpuOption
import net.postchain.mc.cli.util.containerUnitIoReadOption
import net.postchain.mc.cli.util.containerUnitIoWriteOption
import net.postchain.mc.cli.util.containerUnitRamOption
import net.postchain.mc.cli.util.containerUnitStorageOption
import net.postchain.mc.cli.util.extraStorageOption
import net.postchain.mc.cli.util.maxNodes
import net.postchain.mc.cli.util.nameOrGenerateOption
import net.postchain.mc.cli.util.pubkeysOrVotersetOption
import net.postchain.mc.cli.util.systemContainerUnitsOption
import net.postchain.mc.compatibility.ApiCompatV28.ClusterQuotaDataV28
import net.postchain.mc.compatibility.ApiCompatV28.createClusterFromWithClusterQuotaDataDataOperationV28
import net.postchain.mc.compatibility.ApiCompatV28.createClusterWithClusterQuotaDataOperationV28
import net.postchain.mc.compatibility.ApiCompatV33
import net.postchain.mc.compatibility.ApiCompatV33.createClusterFromWithClusterDataOperationV33
import net.postchain.mc.compatibility.ApiCompatV33.createClusterWithClusterDataOperationV33
import net.postchain.mc.compatibility.ApiCompatV87
import net.postchain.mc.compatibility.ApiCompatV87.createClusterFromWithClusterDataOperationV87
import net.postchain.mc.compatibility.ApiCompatV87.createClusterWithClusterDataOperationV87
import net.postchain.mc.compatibility.ApiCompatV97
import net.postchain.mc.compatibility.ApiCompatV97.createClusterFromWithClusterDataOperationV97
import net.postchain.mc.compatibility.ApiCompatV97.createClusterWithClusterDataOperationV97

class CommandAddCluster : DCBaseCommand(
        name = "add",
        help = "Create a new cluster that can hold containers with blockchains. " +
                "This cluster will not be tracked by economy chain and nodes running in it will not be rewarded. " +
                "To create a cluster that is managed by economy chain please see command: 'pmc economy add-cluster'"
) {
    private val name by nameOrGenerateOption("Cluster name")

    private val providerOptions by pubkeysOrVotersetOption()

    private val clusterUnits by clusterUnitsOption().default(1)

    private val extraStorage by extraStorageOption().default(0)

    private val clusterClass by option("-clcl", "--cluster-class", help = "Cluster Class tag (for API v29-32)").default("")

    private val governorName by option(
            "-g", "--governor",
            help = "Name of another voter set which can update this cluster."
    ).required()

    private val systemContainerUnits by systemContainerUnitsOption()
    private val containerUnitCpu by containerUnitCpuOption()
    private val containerUnitRam by containerUnitRamOption()
    private val containerUnitStorage by containerUnitStorageOption()
    private val containerUnitIoRead by containerUnitIoReadOption()
    private val containerUnitIoWrite by containerUnitIoWriteOption()

    private val maxNodes by maxNodes()

    override fun runDC() {
        val apiVersion = client.apiVersion()
        var hasDirectCluster = true
        if (apiVersion >= 49)
            hasDirectCluster = client.hasDirectCluster()
        if (hasDirectCluster) {
            transactionBuilder()
                    .apply {
                        when {
                            apiVersion >= 98 -> {
                                val clusterCreationData = ClusterCreationData(clusterUnits, extraStorage, systemContainerUnits,
                                        containerUnitCpu, containerUnitRam, containerUnitIoRead, containerUnitIoWrite, containerUnitStorage, maxNodes)
                                when (providerOptions) {
                                    is VoterSetOrPubkeysOption.Pubkeys -> {
                                        createClusterWithClusterDataOperation(clientProviderPubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.Pubkeys).pubkeys, clusterCreationData)
                                    }
                                    is VoterSetOrPubkeysOption.VoterSet -> {
                                        createClusterFromWithClusterDataOperation(clientProviderPubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.VoterSet).data, clusterCreationData)
                                    }
                                }
                            }

                            apiVersion >= 88 -> {
                                val clusterCreationData = ApiCompatV97.ClusterCreationDataV97(clusterUnits, extraStorage, systemContainerUnits,
                                        containerUnitCpu, containerUnitRam, containerUnitIoRead, containerUnitIoWrite, containerUnitStorage)
                                when (providerOptions) {
                                    is VoterSetOrPubkeysOption.Pubkeys -> {
                                        createClusterWithClusterDataOperationV97(clientProviderPubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.Pubkeys).pubkeys, clusterCreationData)
                                    }

                                    is VoterSetOrPubkeysOption.VoterSet -> {
                                        createClusterFromWithClusterDataOperationV97(clientProviderPubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.VoterSet).data, clusterCreationData)
                                    }
                                }
                            }

                            apiVersion >= 34 -> {
                                val clusterCreationData = ApiCompatV87.ClusterCreationDataV87(clusterUnits, extraStorage)
                                when (providerOptions) {
                                    is VoterSetOrPubkeysOption.Pubkeys -> {
                                        createClusterWithClusterDataOperationV87(clientProviderPubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.Pubkeys).pubkeys, clusterCreationData)
                                    }

                                    is VoterSetOrPubkeysOption.VoterSet -> {
                                        createClusterFromWithClusterDataOperationV87(clientProviderPubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.VoterSet).data, clusterCreationData)
                                    }
                                }
                            }

                            apiVersion >= 29 -> {
                                when (providerOptions) {
                                    is VoterSetOrPubkeysOption.Pubkeys -> {
                                        createClusterWithClusterDataOperationV33(clientProviderPubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.Pubkeys).pubkeys, ApiCompatV33.ClusterCreationDataV33(clusterUnits, extraStorage, clusterClass))
                                    }

                                    is VoterSetOrPubkeysOption.VoterSet -> {
                                        createClusterFromWithClusterDataOperationV33(clientProviderPubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.VoterSet).data, ApiCompatV33.ClusterCreationDataV33(clusterUnits, extraStorage, clusterClass))
                                    }
                                }
                            }

                            apiVersion >= 24 -> {
                                when (providerOptions) {
                                    is VoterSetOrPubkeysOption.Pubkeys -> {
                                        createClusterWithClusterQuotaDataOperationV28(clientProviderPubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.Pubkeys).pubkeys, ClusterQuotaDataV28(clusterUnits, extraStorage))
                                    }

                                    is VoterSetOrPubkeysOption.VoterSet -> {
                                        createClusterFromWithClusterQuotaDataDataOperationV28(clientProviderPubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.VoterSet).data, ClusterQuotaDataV28(clusterUnits, extraStorage))
                                    }
                                }
                            }

                            apiVersion >= 3 -> {
                                when (providerOptions) {
                                    is VoterSetOrPubkeysOption.Pubkeys -> {
                                        createClusterWithUnitsOperation(clientProviderPubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.Pubkeys).pubkeys, clusterUnits)
                                    }

                                    is VoterSetOrPubkeysOption.VoterSet -> {
                                        createClusterFromWithUnitsOperation(clientProviderPubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.VoterSet).data, clusterUnits)
                                    }
                                }
                            }

                            else -> {
                                when (providerOptions) {
                                    is VoterSetOrPubkeysOption.Pubkeys -> {
                                        createClusterOperation(clientProviderPubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.Pubkeys).pubkeys)
                                    }

                                    is VoterSetOrPubkeysOption.VoterSet -> {
                                        createClusterFromOperation(clientProviderPubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.VoterSet).data)
                                    }
                                }
                            }
                        }
                    }
                    .postAwaitConfirmation(txListener())
                    .printResult(
                            "Cluster $name added",
                            "Could not create cluster"
                    )
        } else {
            throw CliktError("Network is configured to work with EC. Use EC commands to create clusters instead.")
        }
    }
}

