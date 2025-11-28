package net.postchain.mc.cli.cluster

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_cluster.proposeClusterLimitsOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.clusterUnitsOption
import net.postchain.mc.cli.util.extraStorageOption
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption


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

    override fun runDC() {
        transactionBuilder()
                .proposeClusterLimitsOperation(clientProviderPubkey, clusterName, clusterUnits, extraStorage, description)
                .postOrSave()
                .printResult(
                        "Cluster limits proposed",
                        "Failed proposing new cluster limits")
    }
}