package net.postchain.mc.cli.blockchain

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.table.ColumnWidth
import net.postchain.chain0.common.queries.getBlockchainInfoList
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.base.RID_LENGTH
import net.postchain.mc.cli.includeInactiveOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.compatibility.ApiCompatV3.getBlockchainInfoListV3

class CommandListBlockchains : CliktCommand(
        name = "list",
        help = "List blockchains"
) {
    private val config by pmcConfigOption()

    private val includeInactive by includeInactiveOption()

    override fun run() {
        val client = config.client
        val apiVersion = client.apiVersion()
        when {
            apiVersion >= 4 -> {
                val blockchains = client.getBlockchainInfoList(includeInactive)
                if (blockchains.isEmpty()) {
                    echo("No blockchains")
                } else {
                    echo("Blockchains:")
                    echo(defaultTable {
                        column(1) {
                            width = ColumnWidth.Fixed(RID_LENGTH + 1)
                        }
                        header { row("Name", "Rid", "State", "Container", "Cluster") }
                        body {
                            blockchains.forEach {
                                row(it.name, it.rid.toHex(), it.state, it.container ?: "N/A", it.cluster ?: "N/A")
                            }
                        }
                    })
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