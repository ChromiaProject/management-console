package net.postchain.mc.cli.provider.keys

import net.postchain.chain0.common.operations.addProviderKeyOperation
import net.postchain.chain0.provider_auth.model.ProviderKeyRole
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.DIRECTORY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER
import net.postchain.mc.cli.util.pubkeyOption
import net.postchain.mc.compatibility.ApiCompatV87.addProviderKeyOperationV87

class CommandAddProviderKey : DCBaseCommand(
        name = "add",
        help = "Add a key to be used for signing provider transactions",
        requiresVersion = 65,
) {
    private val pubkey by pubkeyOption(helpMsg = "Pubkey to be added to provider")

    override fun runDC() {
        when {
            dcVersion < DIRECTORY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER -> {
                client.transactionBuilder()
                        .addProviderKeyOperationV87(ProviderKeyRole.main, pubkey)
                        .postAwaitConfirmation()
                        .printResult(
                                "Key added as provider key",
                                "Failed to add provider key"
                        )
            }

            else -> {
                client.transactionBuilder()
                        .addProviderKeyOperation(clientProviderPubkey, ProviderKeyRole.main, pubkey)
                        .postAwaitConfirmation()
                        .printResult(
                                "Key added as provider key",
                                "Failed to add provider key"
                        )
            }
        }
    }
}
