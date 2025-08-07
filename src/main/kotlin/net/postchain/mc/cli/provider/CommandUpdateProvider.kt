package net.postchain.mc.cli.provider

import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.operations.updateProviderOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.metadataTextValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.urlOption


class CommandUpdateProvider : DCBaseCommand(
        name = "update",
        help = "Update provider information"
) {
    private val name by nameOption("Provider name").required().validate(metadataTextValidator())

    private val url by urlOption("Provider url")

    override fun runDC() {
        client.transactionBuilder()
                .updateProviderOperation(clientProviderPubkey, name, url)
                .postAwaitConfirmation(txListener())
                .printResult("Information updated", "Could not update provider data")
    }
}
