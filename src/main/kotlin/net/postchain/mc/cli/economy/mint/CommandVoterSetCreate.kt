package net.postchain.mc.cli.economy.mint

import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.createChromiaFoundationVoterSetOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.economy.ECBaseCommand
import net.postchain.mc.cli.economy.ECONOMY_CHAIN_MINTING_VERSION

class CommandVoterSetCreate : ECBaseCommand(
        name = "voter-set-create",
        help = """
            Creates the voter set with members to manage minting proposals. Operation can only be run once and adds the signer as first member.
        """.trimIndent(),
        requiresECVersion = ECONOMY_CHAIN_MINTING_VERSION,
        printHelpOnEmptyArgs = false
) {

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        economyChainClient.transactionBuilder()
                .createChromiaFoundationVoterSetOperation()
                .postAwaitConfirmation()
                .printResult(
                        "Voter set created successfully",
                        "Operation failed"
                )
    }
}