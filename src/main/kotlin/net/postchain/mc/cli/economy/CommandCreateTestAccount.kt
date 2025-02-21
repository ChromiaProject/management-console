package net.postchain.mc.cli.economy

import net.postchain.client.core.PostchainClient
import net.postchain.common.toHex
import net.postchain.economy.economy_chain_test_claim_tchr.createAccountOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.evmAddressOption

class CommandCreateTestAccount : ECBaseCommand(
        name = "create-test-account",
        help = "Create account on Chromia Testnet. Not available on Chromia Mainnet.",
) {
    override val hiddenFromHelp: Boolean = true

    val evmAddress by evmAddressOption()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        economyChainClient.transactionBuilder()
                .createAccountOperation(evmAddress)
                .postAwaitConfirmation()
                .printResult(
                        "Account created for EVM address 0x${evmAddress.toHex()}",
                        "Failed to create account for EVM address 0x${evmAddress.toHex()}"
                )
    }
}
