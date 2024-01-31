package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.economy.economy_chain.createTagOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption

class CommandAddTag : CliktCommand(
        name = "add-tag",
        help = "Add a new tag"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOption("Name of the new tag").required()

    private val scuPrice by option("-scup", "--scu-price", help = "SCU price for the new tag").long().required()

    private val extraStoragePrice by option("-esp", "--extra-storage-price", help = "Extra storage price for the new tag")
            .long().required()

    override fun run() {
        val economyChainClient = getEconomyChainClient(client, config.config)

        economyChainClient.transactionBuilder()
                .createTagOperation(name, scuPrice, extraStoragePrice)
                .postAwaitConfirmation()
                .printResult(
                        "Tag $name has been created",
                        "Failed to create tag"
                )
    }
}