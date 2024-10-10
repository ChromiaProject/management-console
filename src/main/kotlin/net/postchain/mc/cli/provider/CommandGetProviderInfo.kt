package net.postchain.mc.cli.provider

import com.github.ajalt.clikt.core.CliktCommand
import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.table.SectionBuilder
import net.postchain.chain0.common.queries.getNodesByProvider
import net.postchain.chain0.common.queries.getProviderClusters
import net.postchain.chain0.common.queries.getProviderData
import net.postchain.chain0.common.queries.getProviderPoints
import net.postchain.chain0.version.apiVersion
import net.postchain.client.core.PostchainClient
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.optionalPubkeyOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatV47.getProviderData47

class CommandGetProviderInfo : PmcCommand(
        name = "info",
        help = "Show provider information"
) {
    private val config by pmcConfigOption()
    val client get() = config.client

    private val pubkey by optionalPubkeyOption()

    override fun run() {
        val providerPubkey = pubkey ?: client.config.pubkey()
        showProviderInfo(client, providerPubkey)
    }
}

fun CliktCommand.showProviderInfo(client: PostchainClient, pubkey: PubKey) {
    val actionPoints = client.getProviderPoints(pubkey)
    val providerClusters = client.getProviderClusters(pubkey)
    val nodesByProvider = client.getNodesByProvider(pubkey)
    echo(pmcTable {
        body {
            if (client.apiVersion() < 47L) {
                fillProviderData47(client, pubkey)
            } else {
                fillProviderData(client, pubkey)
            }
            row("Action points:", actionPoints.toString())
            row("Belongs to cluster(s)", providerClusters.joinToString(","))
            nodesByProvider.forEachIndexed { index, node ->
                row("Node $index", "${node.pubkey.toHex()} - ${node.host}")
            }
        }
    })
}

internal fun SectionBuilder.fillProviderData47(client: PostchainClient, pubkey: PubKey) {
    val providerData = client.getProviderData47(pubkey)
    row("Provider:", providerData.name)
    row("Url:", providerData.url)
    row("Pubkey:", providerData.pubkey.toHex())
    row("System:", providerData.system.toString())
    row("Tier:", providerData.tier.toString())
    row("Active:", providerData.active.toString())
}

internal fun SectionBuilder.fillProviderData(client: PostchainClient, pubkey: PubKey) {
    val providerData = client.getProviderData(pubkey)
    row("Provider:", providerData.name)
    row("Url:", providerData.url)
    row("Pubkey:", providerData.pubkey.toHex())
    row("System:", providerData.system.toString())
    row("Tier:", providerData.tier.toString())
    row("Active:", providerData.active.toString())
}