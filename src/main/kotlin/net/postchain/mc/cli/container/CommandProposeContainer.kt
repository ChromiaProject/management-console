package net.postchain.mc.cli.container

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.direct_container.createContainerFromOperation
import net.postchain.chain0.direct_container.createContainerFromWithResourceLimitsOperation
import net.postchain.chain0.direct_container.createContainerFromWithUnitsOperation
import net.postchain.chain0.direct_container.createContainerOperation
import net.postchain.chain0.direct_container.createContainerWithResourceLimitsOperation
import net.postchain.chain0.direct_container.createContainerWithUnitsOperation
import net.postchain.chain0.features.hasDirectContainer
import net.postchain.chain0.model.ContainerResourceLimitType
import net.postchain.chain0.proposal_container.proposeContainerOperation
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.VoterSetOrPubkeysOption
import net.postchain.mc.cli.util.containerUnitsOption
import net.postchain.mc.cli.util.extraStorageOption
import net.postchain.mc.cli.util.maxBlockchainsOption
import net.postchain.mc.cli.util.nameOrGenerateOption
import net.postchain.mc.cli.util.nullableProposalDescriptionOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pubkeysOrVotersetOption


class CommandProposeContainer : CliktCommand(
        name = "add",
        help = """
            Propose a new container in an existing cluster 
            
            This also gives authority to deployer voter set to deploy blockchains in it.
        """.trimIndent()
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOrGenerateOption("Container name")

    private val clusterName by option(
            "-c", "--cluster",
            help = "Name of cluster to put container in. Must exist in database"
    ).required()

    private val consensusThreshold by option(
            "--consensus-threshold",
            help = "Consensus threshold: majority (-1), super majority (0, default) or custom (1..deployers.size)"
    ).long().default(0)

    private val deployerOption by pubkeysOrVotersetOption()

    private val containerUnits by containerUnitsOption().default(1)

    private val maxBlockchains by maxBlockchainsOption().default(10)

    private val extraStorage by extraStorageOption().default(0)

    private val description by nullableProposalDescriptionOption()

    private fun description() = description ?: run {
        "Add container $name to the cluster $clusterName - consensus-threshold: $consensusThreshold, " +
                deployerOption.let {
                    if (deployerOption is VoterSetOrPubkeysOption.Pubkeys) "pubkeys: ${(deployerOption as VoterSetOrPubkeysOption.Pubkeys).data}, "
                    else "voter-set: ${(deployerOption as VoterSetOrPubkeysOption.VoterSet).data}, "
                } +
                "container-units: $containerUnits, max-blockchains: $maxBlockchains, extra-storage: $extraStorage"
    }

    private val direct by option("-d", "--direct", help = "Create directly without proposal")
            .flag("-p", "--proposal", default = true)

    override fun run() {
        if (direct) {
            val apiVersion = client.apiVersion()
            var hasDirectContainer = true
            if (apiVersion >= 49)
                hasDirectContainer = client.hasDirectContainer()
            if (hasDirectContainer) {
                client.transactionBuilder()
                        .apply {
                            when {

                                apiVersion >= 24 -> {
                                    when (deployerOption) {
                                        is VoterSetOrPubkeysOption.Pubkeys -> {
                                            createContainerWithResourceLimitsOperation(
                                                    client.pubkey,
                                                    name,
                                                    clusterName,
                                                    consensusThreshold,
                                                    (deployerOption as VoterSetOrPubkeysOption.Pubkeys).pubkeys,
                                                    mapOf(
                                                            ContainerResourceLimitType.container_units to containerUnits,
                                                            ContainerResourceLimitType.max_blockchains to maxBlockchains,
                                                            ContainerResourceLimitType.extra_storage to extraStorage
                                                    )
                                            )
                                        }

                                        is VoterSetOrPubkeysOption.VoterSet -> {
                                            createContainerFromWithResourceLimitsOperation(
                                                    client.pubkey,
                                                    name,
                                                    clusterName,
                                                    consensusThreshold,
                                                    (deployerOption as VoterSetOrPubkeysOption.VoterSet).data,
                                                    mapOf(
                                                            ContainerResourceLimitType.container_units to containerUnits,
                                                            ContainerResourceLimitType.max_blockchains to maxBlockchains,
                                                            ContainerResourceLimitType.extra_storage to extraStorage
                                                    )
                                            )
                                        }
                                    }
                                }

                                apiVersion >= 3 -> {
                                    when (deployerOption) {
                                        is VoterSetOrPubkeysOption.Pubkeys -> {
                                            createContainerWithUnitsOperation(
                                                    client.pubkey,
                                                    name,
                                                    clusterName,
                                                    consensusThreshold,
                                                    (deployerOption as VoterSetOrPubkeysOption.Pubkeys).pubkeys,
                                                    containerUnits
                                            )
                                        }

                                        is VoterSetOrPubkeysOption.VoterSet -> {
                                            createContainerFromWithUnitsOperation(
                                                    client.pubkey,
                                                    name,
                                                    clusterName,
                                                    consensusThreshold,
                                                    (deployerOption as VoterSetOrPubkeysOption.VoterSet).data,
                                                    containerUnits
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    when (deployerOption) {
                                        is VoterSetOrPubkeysOption.Pubkeys -> {
                                            createContainerOperation(
                                                    client.pubkey,
                                                    name,
                                                    clusterName,
                                                    consensusThreshold,
                                                    (deployerOption as VoterSetOrPubkeysOption.Pubkeys).pubkeys
                                            )
                                        }

                                        is VoterSetOrPubkeysOption.VoterSet -> {
                                            createContainerFromOperation(
                                                    client.pubkey,
                                                    name,
                                                    clusterName,
                                                    consensusThreshold,
                                                    (deployerOption as VoterSetOrPubkeysOption.VoterSet).data
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        .postAwaitConfirmation()
                        .printResult(
                                "Container $name has been created",
                                "Failed to create container"
                        )
            } else {
                throw CliktError("""
                    Direct container creation is not supported. Please create your container via economy chain.
                    Note: if you are cluster governor you can also create a proposal for container creation.
                """.trimIndent())
            }
        } else {
            if (deployerOption is VoterSetOrPubkeysOption.Pubkeys) {
                throw CliktError("Container proposals does not support specifying public keys as deployer. Specify a voter set instead.")
            }
            echo("Proposing container. Please note that any specified container limits are ignored. Create a separate proposal to change them from defaults.")
            client.transactionBuilder().proposeContainerOperation(
                    client.pubkey,
                    clusterName,
                    name,
                    (deployerOption as VoterSetOrPubkeysOption.VoterSet).data,
                    description()
            )
                    .postAwaitConfirmation()
                    .printResult(
                            "Container creation has been proposed",
                            "Failed to propose container creation"
                    )
        }
    }

}
