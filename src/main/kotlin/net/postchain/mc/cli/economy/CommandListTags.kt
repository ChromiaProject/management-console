package net.postchain.mc.cli.economy

import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.getTags
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.compatibility.ApiCompatECV66.getTagsECV66

class CommandListTags : ECBaseCommand(
        name = "list-tags",
        help = "List all existing tags",
        printHelpOnEmptyArgs = false
) {
    private val headers = listOf("Name", "SCU price", "Extra storage price", "Extra compute request price")

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        val rows = when {
            ecVersion.version < ECONOMY_CHAIN_TAG_COMPUTE_REQUEST_PRICE_VERSION -> {
                economyChainClient.getTagsECV66()
                        .map {
                            listOf(
                                    it.name,
                                    formatUsd(it.scuPrice),
                                    formatUsd(it.extraStoragePrice),
                            )
                        }
            }
            else -> {
                economyChainClient.getTags()
                        .map {
                            listOf(
                                    it.name,
                                    formatUsd(it.scuPrice),
                                    formatUsd(it.extraStoragePrice),
                                    formatUsd(it.extraComputeRequestPrice),
                            )
                        }
            }
        }
        echo(pmcTable(
                "tags",
                headers,
                rows
        ))
    }
}
