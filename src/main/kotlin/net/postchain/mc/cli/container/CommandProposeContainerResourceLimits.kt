package net.postchain.mc.cli.container

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.model.ContainerResourceLimitType
import net.postchain.chain0.proposal_container.proposal_container_limits.proposeContainerLimitsOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.cluster.CommandProposeClusterResourceLimits.Companion.setIfNotNull
import net.postchain.mc.cli.util.containerUnitsOption
import net.postchain.mc.cli.util.extraStorageOption
import net.postchain.mc.cli.util.maxBlockchainsOption
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption


class CommandProposeContainerResourceLimits : DCBaseCommand(
        name = "limits",
        help = """
            Propose new resource limits for given container
            
            There are multiple types of limits. 
            Proposal can contain all types of limits or a subset of them.
        """.trimIndent()
) {
    private val containerName by nameOption("Container name").required()

    private val containerUnits by containerUnitsOption()

    private val maxBlockchains by maxBlockchainsOption()

    private val extraStorage by extraStorageOption()

    private val description by proposalDescriptionOption {
        "Update container resource limits for $containerName - container-units: $containerUnits, max-blockchains: $maxBlockchains, extra-storage: $extraStorage (MiB)"
    }

    override fun runDC() {
        if (containerUnits == null && maxBlockchains == null && extraStorage == null) {
            echo("No resource limits are specified. At least one value should be specified.")
            return
        }

        val limits = mutableMapOf<ContainerResourceLimitType, Long>()
                .apply {
                    setIfNotNull(ContainerResourceLimitType.container_units, containerUnits)
                    setIfNotNull(ContainerResourceLimitType.max_blockchains, maxBlockchains)
                    setIfNotNull(ContainerResourceLimitType.extra_storage, extraStorage)
                }

        transactionBuilder()
                .proposeContainerLimitsOperation(clientProviderPubkey, containerName, limits, description)
                .postOrSave()
                .printResult(
                        "Container limits proposed",
                        "Failed proposing new container limits")
    }
}