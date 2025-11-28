package net.postchain.mc.cli.provider

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.table.SectionBuilder
import net.postchain.chain0.common.queries.getNodesByProvider
import net.postchain.chain0.common.queries.getProviderClusters
import net.postchain.chain0.common.queries.getProviderData
import net.postchain.chain0.common.queries.getProviderPoints
import net.postchain.client.core.PostchainReadClient
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.util.optionalPubkeyOption
import net.postchain.mc.cli.util.pmcTable

class CommandGetProviderInfo : DCBaseCommand(
        name = "info",
        help = "Show provider information",
        printHelpOnEmptyArgs = false,
) {
    private val pubkey by optionalPubkeyOption()

    override fun runDC() {
        val providerPubkey = pubkey ?: PubKey(clientProviderPubkey)
        showProviderInfo(client, providerPubkey)
    }
}

fun CliktCommand.showProviderInfo(client: PostchainReadClient, pubkey: PubKey) {
    val actionPoints = client.getProviderPoints(pubkey)
    val providerClusters = client.getProviderClusters(pubkey)
    val nodesByProvider = client.getNodesByProvider(pubkey)
    echo(pmcTable {
        body {
            fillProviderData(client, pubkey)
            row("Action points:", actionPoints.toString())
            row("Belongs to cluster(s)", providerClusters.joinToString(","))
            nodesByProvider.forEachIndexed { index, node ->
                row("Node $index", "${node.pubkey.toHex()} - ${node.host}")
            }
        }
    })
}

internal fun SectionBuilder.fillProviderData(client: PostchainReadClient, pubkey: PubKey) {
    val providerData = client.getProviderData(pubkey)
    row("Provider:", providerData.name)
    row("Url:", providerData.url)
    row("Pubkey:", providerData.pubkey.toHex())
    row("System:", providerData.system.toString())
    row("Tier:", providerData.tier.toString())
    row("Active:", providerData.active.toString())
}
