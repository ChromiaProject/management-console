package net.postchain.mc.cli.container

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getContainerBlockchain
import net.postchain.chain0.common.queries.getContainerData
import net.postchain.chain0.nm_api.nmGetContainerLimits
import net.postchain.chain0.version.apiVersion
import net.postchain.client.core.PostchainClient
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV2
import net.postchain.mc.compatibility.ApiCompatV3.getContainerBlockchainV3

class CommandGetContainerInfo : CliktCommand(
        name = "info",
        help = "Get information about a container"
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOption("Container Name").required()

    override fun run() {
        showContainerInfo(client, name)
    }
}

fun CliktCommand.showContainerInfo(client: PostchainClient, name: String) {
    val info = client.getContainerData(name)

    echo(pmcTable {
        body {
            row("Name:", info.name)
            row("Cluster:", info.cluster)
            row("Deployer:", info.deployer)
            row(
                    "Proposed by:",
                    listOf(info.proposedByPubkey.toHex(), info.proposedByName)
                            .filter { it.isNotEmpty() }
                            .joinToString(" / ")
            )
            row("System:", info.system.toString())
            info.state?.let { row("State:", it.toString()) }
        }
    })

    val limits = client.nmGetContainerLimits(name)
    echo(pmcTable(
            "resources limits",
            listOf("Resource type", "Value"),
            limits.map {
                listOf(it.key, when (it.key) {
                    ApiCompatV2.ContainerResourceLimitType.cpu.name -> "${it.value} %"
                    ApiCompatV2.ContainerResourceLimitType.ram.name -> "${it.value} MiB"
                    ApiCompatV2.ContainerResourceLimitType.storage.name -> "${it.value} MiB"
                    ApiCompatV2.ContainerResourceLimitType.io_read.name -> "${it.value} MiB/s"
                    ApiCompatV2.ContainerResourceLimitType.io_write.name -> "${it.value} MiB/s"
                    else -> it.value.toString()
                })
            }
    ))

    val apiVersion = client.apiVersion()
    when {
        apiVersion >= 4 -> {
            val blockchains = client.getContainerBlockchain(name)
            echo(pmcTable(
                    "blockchains",
                    listOf("Name", "Rid", "System", "State"),
                    blockchains.map {
                        listOf(it.name, it.rid.toHex(), it.system.toString(), it.state.toString())
                    }
            ))
        }

        else -> {
            val blockchains = client.getContainerBlockchainV3(name)
            echo(pmcTable(
                    "blockchains",
                    listOf("Name", "Rid", "System", "Active"),
                    blockchains.map {
                        listOf(it.name, it.rid.toHex(), it.system.toString(), it.active.toString())
                    }
            ))
        }
    }
}
