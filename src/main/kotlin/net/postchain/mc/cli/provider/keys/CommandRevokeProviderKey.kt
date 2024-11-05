package net.postchain.mc.cli.provider.keys

import net.postchain.chain0.common.revokeProviderKeyOperation
import net.postchain.chain0.provider_auth.model.ProviderKeyRole
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.pubkeyOption

class CommandRevokeProviderKey : DCBaseCommand(
        name = "revoke",
        help = "Revoke a provider key",
        requiresVersion = 69,
) {
    private val pubkey by pubkeyOption(helpMsg = "Pubkey to be revoked from provider")

    override fun runDC() {

        client.transactionBuilder()
                .revokeProviderKeyOperation(ProviderKeyRole.main, pubkey)
                .postAwaitConfirmation()
                .printResult(
                        "Provider key revoked",
                        "Failed to revoke provider key"
                )
    }
}
