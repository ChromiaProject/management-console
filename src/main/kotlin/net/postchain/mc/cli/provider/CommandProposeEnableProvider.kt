package net.postchain.mc.cli.provider

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.proposal_provider.proposeProviderStateOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.pubkeyOption

class CommandProposeEnableProvider : PmcCommand(
        name = "enable",
        help = "Propose enabling an existing provider"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val key by pubkeyOption()

    private val description by proposalDescriptionOption { "Enable provider $key" }

    override fun run() {
        client.transactionBuilder()
                .proposeProviderStateOperation(client.config.pubkey().data, key.data, true, description)
                .postAwaitConfirmation()
                .printResult(
                        "Enabling of provider has been proposed",
                        "Cannot propose enabling of provider"
                )
    }
}