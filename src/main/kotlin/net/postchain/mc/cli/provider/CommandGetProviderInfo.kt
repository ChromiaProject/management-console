package net.postchain.mc.cli.provider

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getNodesByProvider
import net.postchain.chain0.common.queries.getProviderClusters
import net.postchain.chain0.common.queries.getProviderData
import net.postchain.chain0.common.queries.getProviderPoints
import net.postchain.client.core.PostchainClient
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pubkeyOption

class CommandGetProviderInfo : CliktCommand(
        name = "info",
        help = "Show provider information"
) {
    private val config by pmcConfigOption()
    val client get() = config.client

    private val pubkey by pubkeyOption()

    override fun run() {
        val providerPubkey = pubkey ?: client.config.pubkey()
        showProviderInfo(client, providerPubkey)
    }
}

fun CliktCommand.showProviderInfo(client: PostchainClient, pubkey: PubKey) {
    val providerData = client.getProviderData(pubkey)
    val actionPoints = client.getProviderPoints(pubkey)
    val providerClusters = client.getProviderClusters(pubkey)
    val nodesByProvider = client.getNodesByProvider(pubkey)
    echo(defaultTable {
        body {
            row("Provider:", providerData.name)
            row("Url:", providerData.url)
            row("Pubkey:", providerData.pubkey.toHex())
            row("System:", providerData.system.toString())
            row("Tier:", providerData.tier.toString())
            row("Active:", providerData.active.toString())
            row("Action points:", actionPoints.toString())
            row("Belongs to cluster(s)", providerClusters.joinToString(","))
            nodesByProvider.forEachIndexed { index, node ->
                row("Node $index", "${node.pubkey.toHex()} - ${node.host}")
            }
        }
    })
}
