package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getBlockchainInfoList
import net.postchain.chain0.version.apiVersion
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.base.RID_LENGTH
import net.postchain.mc.cli.includeInactiveOption
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV3.getBlockchainInfoListV3

class CommandListBlockchains : CliktCommand(
        name = "list",
        help = "List blockchains"
) {
    private val config by pmcConfigOption()
    private val interactive by interactiveOption()

    private val includeInactive by includeInactiveOption()

    private val headers = listOf("Name", "Rid", "State", "Container", "Cluster")

    override fun run() {
        val client = config.client
        val apiVersion = client.apiVersion()
        if (interactive && apiVersion < 17) {
            throw CliktError("--interactive requires directory chain version 17, found version $apiVersion")
        }

        when {
            apiVersion >= 4 -> {
                val blockchains = client.getBlockchainInfoList(includeInactive)
                echo(pmcTable(
                        "blockchains",
                        headers,
                        blockchains.map {
                            listOf(it.name, it.rid.toHex(), it.state.toString(), it.container ?: "N/A", it.cluster
                                    ?: "N/A")
                        },
                        1 to RID_LENGTH,
                        interactive))
                if (interactive && blockchains.isNotEmpty()) {
                    promptForIndex(blockchains)?.let {
                        showBlockchainInfo(client, apiVersion, BlockchainRid(blockchains[it].rid))
                    }
                }
            }

            else -> {
                val blockchains = client.getBlockchainInfoListV3(includeInactive)
                echo(pmcTable(
                        "blockchains",
                        listOf("Name", "Rid", "Active", "Container", "Cluster"),
                        blockchains.map { listOf(it.name, it.rid.toHex(), it.active.toString(), it.container, it.cluster) },
                        1 to RID_LENGTH
                ))
            }
        }
    }
}