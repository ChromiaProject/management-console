package net.postchain.mc.cli.blockchain

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getBlockchainInfo
import net.postchain.chain0.version.apiVersion
import net.postchain.client.core.PostchainClient
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.pmcConfigOption

class CommandGetBlockchainInfo : CliktCommand(
        name = "info",
        help = "Get blockchain info"
) {
    private val config by pmcConfigOption()

    private val blockchainRID by blockchainRidOption().required()

    override fun run() {
        val client = config.client
        val apiVersion = client.apiVersion()
        if (apiVersion >= 17) {
            showBlockchainInfo(client, blockchainRID)
        } else {
            throw CliktError("blockchain info requires directory chain version 17, found version $apiVersion")
        }
    }
}

fun CliktCommand.showBlockchainInfo(client: PostchainClient, blockchainRid: BlockchainRid) {
    val blockchainInfo = client.getBlockchainInfo(blockchainRid.data)
    if (blockchainInfo != null) {
        echo(defaultTable {
            body {
                row("Name:", blockchainInfo.name)
                row("Rid:", blockchainInfo.rid)
                row("State:", blockchainInfo.state)
                row("Container:", blockchainInfo.container)
                row("Cluster:", blockchainInfo.cluster)
                row("Is system chain:", blockchainInfo.system)
            }
        })
    } else {
        throw CliktError("Blockchain with rid $blockchainRid not found")
    }
}
