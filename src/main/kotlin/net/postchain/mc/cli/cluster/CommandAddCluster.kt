package net.postchain.mc.cli.cluster

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.direct_cluster.createClusterFromOperation
import net.postchain.chain0.direct_cluster.createClusterFromWithClusterDataOperation
import net.postchain.chain0.direct_cluster.createClusterFromWithUnitsOperation
import net.postchain.chain0.direct_cluster.createClusterOperation
import net.postchain.chain0.direct_cluster.createClusterWithClusterDataOperation
import net.postchain.chain0.direct_cluster.createClusterWithUnitsOperation
import net.postchain.chain0.model.ClusterCreationData
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.VoterSetOrPubkeysOption
import net.postchain.mc.cli.util.clusterUnitsOption
import net.postchain.mc.cli.util.extraStorageOption
import net.postchain.mc.cli.util.nameOrGenerateOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pubkeysOrVotersetOption
import net.postchain.mc.compatibility.ApiCompatV28.ClusterQuotaDataV28
import net.postchain.mc.compatibility.ApiCompatV28.createClusterFromWithClusterQuotaDataDataOperationV28
import net.postchain.mc.compatibility.ApiCompatV28.createClusterWithClusterQuotaDataOperationV28

class CommandAddCluster : CliktCommand(
        name = "add",
        help = "Create a new cluster that can hold containers with blockchains"
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOrGenerateOption("Cluster name")

    private val providerOptions by pubkeysOrVotersetOption()

    private val clusterUnits by clusterUnitsOption().default(1)

    private val extraStorage by extraStorageOption().default(0)

    private val clusterClass by option("-clcl", "--cluster-class", help = "Cluster Class tag").default("")

    private val governorName by option(
            "-g", "--governor",
            help = "Name of another voter set which can update this cluster."
    ).required()

    override fun run() {
        val apiVersion = client.apiVersion()
        client.transactionBuilder()
                .apply {
                    when {
                        apiVersion >= 29 -> {
                            when (providerOptions) {
                                is VoterSetOrPubkeysOption.Pubkeys -> {
                                    createClusterWithClusterDataOperation(client.pubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.Pubkeys).pubkeys, ClusterCreationData(clusterUnits, extraStorage, clusterClass))
                                }

                                is VoterSetOrPubkeysOption.VoterSet -> {
                                    createClusterFromWithClusterDataOperation(client.pubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.VoterSet).data, ClusterCreationData(clusterUnits, extraStorage, clusterClass))
                                }
                            }
                        }

                        apiVersion >= 24 -> {
                            when (providerOptions) {
                                is VoterSetOrPubkeysOption.Pubkeys -> {
                                    createClusterWithClusterQuotaDataOperationV28(client.pubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.Pubkeys).pubkeys, ClusterQuotaDataV28(clusterUnits, extraStorage))
                                }

                                is VoterSetOrPubkeysOption.VoterSet -> {
                                    createClusterFromWithClusterQuotaDataDataOperationV28(client.pubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.VoterSet).data, ClusterQuotaDataV28(clusterUnits, extraStorage))
                                }
                            }
                        }

                        apiVersion >= 3 -> {
                            when (providerOptions) {
                                is VoterSetOrPubkeysOption.Pubkeys -> {
                                    createClusterWithUnitsOperation(client.pubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.Pubkeys).pubkeys, clusterUnits)
                                }

                                is VoterSetOrPubkeysOption.VoterSet -> {
                                    createClusterFromWithUnitsOperation(client.pubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.VoterSet).data, clusterUnits)
                                }
                            }
                        }

                        else -> {
                            when (providerOptions) {
                                is VoterSetOrPubkeysOption.Pubkeys -> {
                                    createClusterOperation(client.pubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.Pubkeys).pubkeys)
                                }

                                is VoterSetOrPubkeysOption.VoterSet -> {
                                    createClusterFromOperation(client.pubkey, name, governorName, (providerOptions as VoterSetOrPubkeysOption.VoterSet).data)
                                }
                            }
                        }
                    }
                }
                .postAwaitConfirmation()
                .printResult(
                        "Cluster $name added",
                        "Could not create cluster"
                )
    }
}

