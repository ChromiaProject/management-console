package net.postchain.mc.cli.economy.mint

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.client.core.PostchainClient
import net.postchain.common.hexStringToByteArray
import net.postchain.economy.economy_chain.proposeMintingOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.economy.ECONOMY_CHAIN_MINTING_VERSION

class CommandMintPropose : ECBaseCommand(
        name = "propose",
        help = "Create a minting proposal",
        requiresECVersion = ECONOMY_CHAIN_MINTING_VERSION
) {
    private val amount by option("--amount", help = "Amount to min")
            .long()
            .required()
    private val accountId by option("--account-id", help = "Destination account id")
            .required()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        economyChainClient.transactionBuilder()
                .proposeMintingOperation(client.pubkey, amount, accountId.hexStringToByteArray())
                .postAwaitConfirmation()
                .printResult(
                        "Minting proposal created successfully",
                        "Operation failed"
                )
    }
}
