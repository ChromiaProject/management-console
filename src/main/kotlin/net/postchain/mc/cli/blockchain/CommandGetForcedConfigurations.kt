package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.ForcedConfigurationData
import net.postchain.chain0.proposal_blockchain.getForcedConfigurations
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandGetForcedConfigurations : PmcCommand(
        name = "get-forced-configurations",
        help = "Get forced configurations"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val headers = listOf("Height", "Config hash")

    override fun run() {
        val forcedConfigurations: List<ForcedConfigurationData> = client.getForcedConfigurations(blockchainRID)
        echo(pmcTable(
                "Forced Configurations",
                headers,
                forcedConfigurations.map {
                    listOf(it.height.toString(), it.configHash.toHex())
                }))
    }
}
