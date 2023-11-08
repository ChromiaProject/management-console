package net.postchain.mc.cli.provider

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import net.postchain.chain0.common.operations.promoteNodeProviderOperation
import net.postchain.chain0.proposal_provider.proposeProviderIsSystemOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.pubkeyOption

class CommandPromoteProvider : CliktCommand(
        name = "promote",
        help = "Gives a provider access to add signer nodes to clusters"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val key by pubkeyOption("Public key of provider to promote")

    private val system by option(help = "Proposes this provider as a system provider").flag()

    private val description by proposalDescriptionOption()

    override fun run() {
        client.transactionBuilder()
                .run {
                    if (system) proposeProviderIsSystemOperation(client.pubkey, key.data, true, description)
                    else promoteNodeProviderOperation(client.pubkey, key.data)
                }
                .postAwaitConfirmation()
                .printResult(
                        "Promotion of provider has been proposed",
                        "Failed to promote provider"
                )
    }
}
