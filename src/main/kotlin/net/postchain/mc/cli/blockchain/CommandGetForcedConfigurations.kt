package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.ForcedConfigurationData
import net.postchain.chain0.proposal_blockchain.getForcedConfigurations
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV68.ForcedConfigurationDataV68
import net.postchain.mc.compatibility.ApiCompatV68.getForcedConfigurationV68
import net.postchain.mc.network.Version

class CommandGetForcedConfigurations : PmcCommand(
        name = "get-forced-configurations",
        help = "Get forced configurations"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val blockchainRID by blockchainRidOption().required()

    private val headers = listOf("Height", "Config hash")

    override fun run() {
        val version = Version(client).version
        when {
            version >= 68 -> {
                val forcedConfigurations: List<ForcedConfigurationData> = client.getForcedConfigurations(blockchainRID)
                echo(pmcTable(
                        "Forced Configurations",
                        headers,
                        forcedConfigurations.map {
                            listOf(it.height.toString(), it.configHash.toHex())
                        }))
            }

            version >= 40 -> {
                val forcedConfigurations: List<ForcedConfigurationDataV68> = client.getForcedConfigurationV68(blockchainRID)
                echo(pmcTable(
                        "Forced Configurations",
                        headers,
                        forcedConfigurations.map {
                            listOf(it.height.toString(), it.configHash.toHex())
                        }))
            }

            else -> {
                throw CliktError("Force update requires directory chain version 40 or higher, found version $version")
            }
        }

    }
}
