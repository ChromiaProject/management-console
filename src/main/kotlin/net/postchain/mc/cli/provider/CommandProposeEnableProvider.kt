package net.postchain.mc.cli.provider

import net.postchain.chain0.proposal_provider.proposeProviderStateOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.pubkeyOption

class CommandProposeEnableProvider : DCBaseCommand(
        name = "enable",
        help = "Propose enabling an existing provider"
) {
    private val key by pubkeyOption()

    private val description by proposalDescriptionOption { "Enable provider $key" }

    override fun runDC() {
        client.transactionBuilder()
                .proposeProviderStateOperation(clientProviderPubkey, key.data, true, description)
                .postAwaitConfirmation()
                .printResult(
                        "Enabling of provider has been proposed",
                        "Cannot propose enabling of provider"
                )
    }
}