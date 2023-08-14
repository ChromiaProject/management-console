package net.postchain.mc.cli.provider

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.management_chain_directory1.updateProviderOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.urlOption


class CommandUpdateProvider : CliktCommand(
        name = "update",
        help = "Update provider information"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val name by nameOption("Provider name").required()

    private val url by urlOption("Provider url")

    override fun run() {
        client.transactionBuilder()
                .updateProviderOperation(client.config.pubkey().data, name, url)
                .postAwaitConfirmation()
                .printResult("Information updated", "Could not update provider data")
    }
}
