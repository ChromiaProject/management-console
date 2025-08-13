package net.postchain.mc.cli.provider

import net.postchain.chain0.proposal_provider.proposeProviderStateOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.pubkeyOption


class CommandProposeDisableProvider : DCBaseCommand(
        name = "disable",
        help = "Propose disabling an existing provider"
) {
    private val key by pubkeyOption()

    private val description by proposalDescriptionOption { "Disable provider $key" }

    override fun runDC() {
        transactionBuilder()
                .proposeProviderStateOperation(clientProviderPubkey, key.data, false, description)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Disabling of provider has been proposed",
                        "Cannot propose disabling of provider"
                )
    }
}
