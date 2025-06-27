package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.removeTagOperation
import net.postchain.mc.compatibility.ApiCompatECV57.removeTagOperation
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption

class CommandRemoveTag  : ECBaseCommand(
    name = "remove-tag",
    help = "Remove tag"
) {
    private val name by nameOption("Name of the tag to be removed").required()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        when {
            ecVersion.version < ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION -> {
                economyChainClient.transactionBuilder()
                        .removeTagOperation(name)
                        .postAwaitConfirmation()
                        .printResult(
                                "Proposal for removing tag $name is created",
                                "Failed to remove tag"
                        )
            }

            else -> {
                economyChainClient.transactionBuilder()
                        .removeTagOperation(clientProviderPubkey, name)
                        .postAwaitConfirmation()
                        .printResult(
                                "Proposal for removing tag $name is created",
                                "Failed to remove tag"
                        )
            }

        }
    }
}