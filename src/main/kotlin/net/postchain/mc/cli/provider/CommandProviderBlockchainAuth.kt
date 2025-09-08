package net.postchain.mc.cli.provider

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.mordant.terminal.prompt
import net.postchain.chain0.blockchain_auth.addProviderBlockchainAuthOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.util.DIRECTORY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER
import net.postchain.mc.compatibility.ApiCompatV87.addProviderBlockchainAuthOperationV87

val warningMessage = """
           |This will disable normal authentication, and you will no longer be able to access the provider as before. 
           |This can only be reverted with blockchain authentication.""".trimMargin()

class CommandProviderBlockchainAuth : DCBaseCommand(
        name = "blockchain-auth",
        help = """
            |Make it possible to use an operation from another blockchain to authenticate this provider.
            |
            |Note: $warningMessage""".trimMargin(),
        requiresVersion = 71
) {
    private val blockchainRid by blockchainRidOption().required()

    override fun runDC() {
        if (terminal.terminalInfo.inputInteractive) {
            val answer = terminal.prompt("$warningMessage\nAre you sure you want to enable blockchain authentication for blockchain\n${blockchainRid.toHex()}\n(y/N)?")
            if (answer == null || !answer.startsWith("Y", ignoreCase = true)) return
        }
        when {
            dcVersion < DIRECTORY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER -> {
                transactionBuilder()
                        .addProviderBlockchainAuthOperationV87(blockchainRid)
                        .postAwaitConfirmation(txListener())
                        .printResult(
                                "Blockchain authentication has been enabled",
                                "Failed to enable blockchain authentication"
                        )
            }

            else -> {
                transactionBuilder()
                        .addProviderBlockchainAuthOperation(clientProviderPubkey, blockchainRid)
                        .postAwaitConfirmation(txListener())
                        .printResult(
                                "Blockchain authentication has been enabled",
                                "Failed to enable blockchain authentication"
                        )
            }
        }
    }
}
