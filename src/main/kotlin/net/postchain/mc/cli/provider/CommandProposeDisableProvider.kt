package net.postchain.mc.cli.provider

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.proposal_provider.proposeProviderStateOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.pubkeyOption


class CommandProposeDisableProvider : PmcCommand(
        name = "disable",
        help = "Propose disabling an existing provider"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val key by pubkeyOption()

    private val description by proposalDescriptionOption { "Disable provider $key" }

    override fun run() {
        client.transactionBuilder()
                .proposeProviderStateOperation(client.config.pubkey().data, key.data, false, description)
                .postAwaitConfirmation()
                .printResult(
                        "Disabling of provider has been proposed",
                        "Cannot propose disabling of provider"
                )
    }
}
