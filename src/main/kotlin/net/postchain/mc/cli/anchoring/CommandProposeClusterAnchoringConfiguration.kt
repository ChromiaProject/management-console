package net.postchain.mc.cli.anchoring

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.proposal_cluster_anchoring.proposeClusterAnchoringConfigurationOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.pmcConfigOption

class CommandProposeClusterAnchoringConfiguration : CliktCommand(
        name = "update",
        help = "Propose new cluster anchoring configuration"
) {
    private val config by pmcConfigOption()

    private val anchoringConfig by option(
            "-ac",
            "--anchoring-config",
            help = "Configuration file for cluster anchoring chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true).required()

    override fun run() {
        val bcConfig = BlockchainConfig.readFromFile(anchoringConfig)
        config.client.transactionBuilder()
                .proposeClusterAnchoringConfigurationOperation(config.client.config.pubkey().data, bcConfig.data)
                .postAwaitConfirmation()
                .printResult(
                        "Cluster anchoring configuration was proposed: ${bcConfig.hash}",
                        "Failed to propose cluster anchoring configuration"
                )
    }
}
