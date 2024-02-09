package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.createTagOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption

class CommandAddTag : ECBaseCommand(
        name = "add-tag",
        help = "Add a new tag"
) {
    private val name by nameOption("Name of the new tag").required()

    private val scuPrice by option("-scup", "--scu-price", help = "SCU price for the new tag").long().required()

    private val extraStoragePrice by option("-esp", "--extra-storage-price", help = "Extra storage price for the new tag")
            .long().required()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        economyChainClient.transactionBuilder()
                .createTagOperation(name, scuPrice, extraStoragePrice)
                .postAwaitConfirmation()
                .printResult(
                        "Proposal for creating tag $name is created",
                        "Failed to create tag"
                )
    }
}