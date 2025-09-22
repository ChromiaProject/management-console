package net.postchain.mc.cli.container

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.proposal_container.proposal_container_configuration.ContainerConfigurationData
import net.postchain.chain0.proposal_container.proposal_container_configuration.proposeContainerConfigurationOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption

class CommandProposeContainerConfiguration : DCBaseCommand(
        name = "configuration",
        help = """
            Propose configurations for given container
        """.trimIndent(),
        requiresVersion = 101
) {
    private val containerName by nameOption("Container name").required()

    private val slowDBStatementLogMs by option("-sdbl", "--slow-db-statement-log-ms", help = "Threshold for slow DB statement log in milliseconds")
            .long()

    private val description by proposalDescriptionOption {
        "Update container configuration for $containerName - slow-db-statement-log-ms: $slowDBStatementLogMs"
    }

    override fun runDC() {
        if (slowDBStatementLogMs == null) {
            throw CliktError("No configurations are specified. At least one configuration should be specified.")
        }

        transactionBuilder()
                .proposeContainerConfigurationOperation(
                        clientProviderPubkey,
                        containerName,
                        ContainerConfigurationData(slowDBStatementLogMs),
                        description
                ).postAwaitConfirmation(txListener())
                .printResult(
                        "Container configuration proposed",
                        "Failed proposing new container configuration"
                )
    }
}