package net.postchain.mc.cli.blockchain

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.ForcedConfigurationData
import net.postchain.chain0.proposal_blockchain.getForcedConfiguration
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.network.Version

class CommandGetForcedConfiguration : PmcCommand(
        name = "get-forced-configuration",
        help = "Get forced configuration"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val headers = listOf("Height", "Config hash")

    override fun run() {
        val version = Version(client)
        if (version.version < 40) {
            echo("Force update requires directory chain version 40, found version $version")
            return
        }

        val forcedConfigurations: List<ForcedConfigurationData> = client.getForcedConfiguration(blockchainRID)

        echo(pmcTable(
                "Forced Configurations",
                headers,
                forcedConfigurations.map {
                    listOf(it.height.toString(), it.configHash.toHex())
                }))
    }
}
