package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.parameters.options.required
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.removeTagOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption

class CommandRemoveTag  : ECBaseCommand(
    name = "remove-tag",
    help = "Remove tag"
) {
    private val name by nameOption("Name of the tag to be removed").required()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        economyChainClient.transactionBuilder()
                .removeTagOperation(name)
                .postAwaitConfirmation()
                .printResult(
                        "Proposal for removing tag $name is created",
                        "Failed to remove tag"
                )
    }
}