package net.postchain.mc.cli.provider.keys

import net.postchain.chain0.common.addProviderKeyOperation
import net.postchain.chain0.provider_auth.model.ProviderKeyRole
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.pubkeyOption

class CommandAddProviderKey : DCBaseCommand(
        name = "add",
        help = "Add a key to be used for signing provider transactions",
        requiresVersion = 65,
) {
    private val pubkey by pubkeyOption(helpMsg = "Pubkey to be added to provider")

    override fun runDC() {

        client.transactionBuilder()
                .addProviderKeyOperation(ProviderKeyRole.main, pubkey)
                .postAwaitConfirmation()
                .printResult(
                        "Key added as provider key",
                        "Failed to add provider key"
                )
    }
}
