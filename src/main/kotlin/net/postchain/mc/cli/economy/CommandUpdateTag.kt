package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.updateTagOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption

class CommandUpdateTag : ECBaseCommand(
        name = "update-tag",
        help = "Update an existing tag"
) {
    private val name by nameOption("Name of the tag").required()

    private val scuPrice by option("-scup", "--scu-price", help = "Updated SCU price for the tag").long()

    private val extraStoragePrice by option("-esp", "--extra-storage-price", help = "Updated extra storage price for the tag")
            .long()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        if (scuPrice == null && extraStoragePrice == null) {
            throw CliktError("Must specify either updated SCU price or extra storage price")
        }

        economyChainClient.transactionBuilder()
                .updateTagOperation(name, scuPrice, extraStoragePrice)
                .postAwaitConfirmation()
                .printResult(
                        "Proposal for updating tag $name is created",
                        "Failed to update tag"
                )
    }
}