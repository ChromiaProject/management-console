package net.postchain.mc.cli.blockchain

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.table.ColumnWidth
import net.postchain.chain0.common.queries.getBlockchainInfoList
import net.postchain.chain0.version.apiVersion
import net.postchain.common.BlockchainRid
import net.postchain.mc.cli.base.RID_LENGTH
import net.postchain.mc.cli.includeInactiveOption
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
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
                if (blockchains.isEmpty()) {
                    echo("No blockchains")
                } else {
                    echo("Blockchains:")
                    echo(defaultTable {
                        if (interactive) {
                            column(0) {
                                width = ColumnWidth.Fixed(3)
                            }
                        } else {
                            column(1) {
                                width = ColumnWidth.Fixed(RID_LENGTH + 1)
                            }
                        }
                        header { rowFrom(if (interactive) listOf("#") + headers else headers) }
                        body {
                            blockchains.forEachIndexed { index, it ->
                                val columns = listOf(it.name, it.rid.toHex(), it.state, it.container ?: "N/A", it.cluster ?: "N/A")
                                rowFrom(if (interactive) listOf(index.toString()) + columns else columns)
                            }
                        }
                    })
                }
                if (interactive) {
                    promptForIndex(blockchains)?.let {
                        showBlockchainInfo(client, BlockchainRid(blockchains[it].rid))
                    }
                }
            }

            else -> {
                val blockchains = client.getBlockchainInfoListV3(includeInactive)
                if (blockchains.isEmpty()) {
                    echo("No blockchains")
                } else {
                    echo("Blockchains:")
                    echo(defaultTable {
                        header { row("Name", "Rid", "Active", "Container", "Cluster") }
                        body {
                            blockchains.forEach {
                                row(it.name, it.rid.toHex(), it.active.toString(), it.container, it.cluster)
                            }
                        }
                    })
                }
            }
        }
    }
}