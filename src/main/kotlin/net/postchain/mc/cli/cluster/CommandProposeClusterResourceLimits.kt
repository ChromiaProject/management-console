package net.postchain.mc.cli.cluster

import com.github.ajalt.clikt.parameters.options.deprecated
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.proposal_cluster.proposeClusterLimitsOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.clusterUnitsOption
import net.postchain.mc.cli.util.extraStorageOption
import net.postchain.mc.cli.util.maxBlockchainsOption
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.compatibility.ApiCompatV2
import net.postchain.mc.compatibility.ApiCompatV2.proposeClusterLimitsOperationV2
import net.postchain.mc.compatibility.ApiCompatV22.proposeClusterLimitsOperationV22


class CommandProposeClusterResourceLimits : DCBaseCommand(
        name = "limits",
        help = "Propose new resource limits for given cluster"
) {
    companion object {
        fun <K> MutableMap<K, Long>.setIfNotNull(key: K, value: Long?) {
            value?.let { put(key, it) }
        }
    }

    private val clusterName by nameOption("Cluster name").required()

    private val clusterUnits by clusterUnitsOption()

    private val extraStorage by extraStorageOption()

    private val description by proposalDescriptionOption {
        "Update cluster resource limits for $clusterName - cluster-units: $clusterUnits, extra-storage: $extraStorage (MiB)"
    }

    // Remove when api version 2 is not needed
    private val _maxContainers by option("-mc", "--max-containers", help = "Max containers per cluster").long().deprecated()
    private val _maxBlockchains by maxBlockchainsOption().deprecated()
    private val _cpu by option("-c", "--cpu", help = "CPU limit (percent of cpus, 10 == 0.1 cpu(s), 150 == 1.5 cpu(s))").long().deprecated()
    private val _ram by option("-r", "--ram", help = "RAM limit (MiB)").long().deprecated()
    private val _storage by option("-st", "--storage", help = "Storage limit (MiB)").long().deprecated()
    private val _ioRead by option("-ir", "--io-read", help = "Disk I/O read limit (MiB/s)").long().deprecated()
    private val _ioWrite by option("-iw", "--io-write", help = "Disk I/O write limit (MiB/s)").long().deprecated()

    override fun runDC() {
        client.transactionBuilder()
                .apply {
                    when {
                        dcVersion >= 24 -> proposeClusterLimitsOperation(clientProviderPubkey, clusterName, clusterUnits, extraStorage, description)
                        dcVersion >= 3 -> proposeClusterLimitsOperationV22(clientProviderPubkey, clusterName, clusterUnits, description)
                        else -> {
                            val limits = mutableMapOf<ApiCompatV2.ClusterResourceLimitType, Long>()
                                    .apply {
                                        setIfNotNull(ApiCompatV2.ClusterResourceLimitType.max_containers, _maxContainers)
                                        setIfNotNull(ApiCompatV2.ClusterResourceLimitType.default_container_max_blockchains, _maxBlockchains)
                                        setIfNotNull(ApiCompatV2.ClusterResourceLimitType.default_container_cpu, _cpu)
                                        setIfNotNull(ApiCompatV2.ClusterResourceLimitType.default_container_ram, _ram)
                                        setIfNotNull(ApiCompatV2.ClusterResourceLimitType.default_container_storage, _storage)
                                        setIfNotNull(ApiCompatV2.ClusterResourceLimitType.default_container_io_read, _ioRead)
                                        setIfNotNull(ApiCompatV2.ClusterResourceLimitType.default_container_io_write, _ioWrite)
                                    }
                            proposeClusterLimitsOperationV2(clientProviderPubkey, clusterName, limits, description)

                        }
                    }
                }
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Cluster limits proposed",
                        "Failed proposing new cluster limits")
    }
}