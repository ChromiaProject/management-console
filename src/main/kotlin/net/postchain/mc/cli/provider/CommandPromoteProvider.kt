package net.postchain.mc.cli.provider

import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import net.postchain.chain0.common.operations.promoteNodeProviderOperation
import net.postchain.chain0.proposal_provider.proposeProviderIsSystemOperation
import net.postchain.chain0.proposal_provider.proposeProviderStateOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.pubkeyOption

class CommandPromoteProvider : DCBaseCommand(
        name = "promote",
        help = "Gives a provider access to add signer nodes to clusters"
) {
    private val key by pubkeyOption("Public key of provider to promote")
    private val enable by option(help = "Adds voting to enable provider if promoting to node provider").flag()

    private val system by option(help = "Proposes this provider as a system provider").flag()

    private val description by proposalDescriptionOption { "Promote provider $key - enable: $enable, system: $system" }

    override fun runDC() {
        client.transactionBuilder()
                .run {
                    if (system) proposeProviderIsSystemOperation(clientProviderPubkey, key.data, true, description)
                    else promoteNodeProviderOperation(clientProviderPubkey, key.data).apply {
                        if (enable) proposeProviderStateOperation(clientProviderPubkey, key.data, enable, description)
                    }
                }
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Promotion of provider has been proposed",
                        "Failed to promote provider"
                )
    }
}
