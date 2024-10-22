package net.postchain.mc.cli.provider.keys

import net.postchain.chain0.common.removeProviderKeyOperation
import net.postchain.chain0.provider_auth.model.ProviderKeyRole
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.pubkeyOption

class CommandRemoveProviderKey : DCBaseCommand(
        name = "remove",
        help = "Remove a provider key",
        requiresVersion = 65,
) {
    private val pubkey by pubkeyOption(helpMsg = "Pubkey to be removed from provider")

    override fun runDC() {

        client.transactionBuilder()
                .removeProviderKeyOperation(ProviderKeyRole.main, pubkey)
                .postAwaitConfirmation()
                .printResult(
                        "Provider key removed",
                        "Failed to remove provider key"
                )
    }
}
