package net.postchain.mc.cli.economy

import com.chromia.cli.tools.ft.addEvmAuthOperation
import com.chromia.cli.tools.ft.findFtAccountIdAndAuthDescriptorId
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.client.core.PostchainClient
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.economy.economy_chain_test_claim_tchr.FAUCET
import net.postchain.economy.economy_chain_test_claim_tchr.faucetOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.evmAddressOption

class CommandClaimTestChr : ECBaseCommand(
        name = "claim-test-chr",
        help = "Claim tCHR on Chromia Testnet. Not available on Chromia Mainnet.",
) {
    val accountIdOption by option("--account-id", help = "Account id of the account claim tCHR to.")
            .validate { it.hexStringToByteArray(); require(it.isNotEmpty()) { "Account id cannot be empty" } }

    val evmAddress by evmAddressOption()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        val (accountId, authDescriptorId) =
                findFtAccountIdAndAuthDescriptorId(economyChainClient, accountIdOption, evmAddress, FAUCET, null)

        economyChainClient.transactionBuilder().also {
            addEvmAuthOperation(
                    economyChainClient, it,
                    FAUCET, listOf(),
                    evmAddress, accountId, authDescriptorId)
            echo("Signing done, posting transaction...")
        }
                .faucetOperation()
                .postAwaitConfirmation()
                .printResult(
                        "tCHR claimed to account ${accountId.toHex()}",
                        "Failed to claim tCHR to account ${accountId.toHex()}"
                )
    }
}
