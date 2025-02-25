package net.postchain.mc.cli.lease

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.getLeaseByContainerName
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.util.pmcTable

class CommandGetLeaseInfo : ECBaseCommand(
        name = "info",
        help = "Get lease info"
) {
    val containerName by option("-n", "--name", help = "Container name", metavar = "name").required()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        showLeaseInfo(economyChainClient, containerName)
    }
}

internal fun CliktCommand.showLeaseInfo(client: PostchainClient, containerName: String) {
    val lease = client.getLeaseByContainerName(containerName)
            ?: throw CliktError("No lease found for container $containerName")

    echo(pmcTable {
        body {
            row("Cluster", lease.clusterName)
            row("Container", lease.containerName)
            row("Container Units (SCU)", lease.containerUnits.toString())
            row("Extra storage (GiB)", lease.extraStorageGib.toString())
            row("Expire Time (millis)", lease.expireTimeMillis.toString())
            row("Expired", lease.expired.toString())
            row("Auto Renewal", lease.autoRenew.toString())
            row("Subnode image", lease.subnodeImageName)
        }
    })
}
