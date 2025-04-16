package net.postchain.mc.cli.provider.keys

import net.postchain.chain0.common.operations.revokeProviderKeyOperation
import net.postchain.chain0.provider_auth.model.ProviderKeyRole
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.DIRECTORY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER
import net.postchain.mc.cli.util.pubkeyOption
import net.postchain.mc.compatibility.ApiCompatV87.revokeProviderKeyOperationV87

class CommandRevokeProviderKey : DCBaseCommand(
        name = "revoke",
        help = "Revoke a provider key",
        requiresVersion = 69,
) {
    private val pubkey by pubkeyOption(helpMsg = "Pubkey to be revoked from provider")

    override fun runDC() {
        when {
            dcVersion < DIRECTORY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER -> {
                client.transactionBuilder()
                        .revokeProviderKeyOperationV87(ProviderKeyRole.main, pubkey)
                        .postAwaitConfirmation()
                        .printResult(
                                "Provider key revoked",
                                "Failed to revoke provider key"
                        )

            }

            else -> {
                client.transactionBuilder()
                        .revokeProviderKeyOperation(clientProviderPubkey, ProviderKeyRole.main, pubkey)
                        .postAwaitConfirmation()
                        .printResult(
                                "Provider key revoked",
                                "Failed to revoke provider key"
                        )
            }
        }
    }
}
