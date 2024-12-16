package net.postchain.mc.cli.economy.mint

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import net.postchain.client.core.PostchainClient
import net.postchain.common.hexStringToByteArray
import net.postchain.economy.economy_chain.updateChromiaFoundationVoterSetOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.economy.ECBaseCommand
import net.postchain.mc.cli.economy.ECONOMY_CHAIN_MINTING_VERSION
import net.postchain.mc.cli.util.thresholdOption

class CommandVoterSetUpdate : ECBaseCommand(
        name = "voter-set-update",
        help = """
            Propose an update of the chromia foundation voter set
        """.trimIndent(),
        requiresECVersion = ECONOMY_CHAIN_MINTING_VERSION
) {
    private val threshold by thresholdOption()
    private val newMember by option("--add-member", help = "Members pubkey(s) to add to voter set")
            .convert { it.hexStringToByteArray() }
            .split(",")
            .default(listOf())
    private val removeMember by option("--remove-member", help = "Members pubkey(s) to remove from voter set")
            .convert { it.hexStringToByteArray() }
            .split(",")
            .default(listOf())

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        if (threshold == null && newMember.isEmpty() && removeMember.isEmpty()) {
            throw CliktError("No update specified")
        }

        economyChainClient.transactionBuilder()
                .updateChromiaFoundationVoterSetOperation(
                        client.config.pubkey().data,
                        threshold,
                        newMember,
                        removeMember
                )
                .postAwaitConfirmation()
                .printResult(
                        "Proposal for chromia foundation voter set has been added",
                        "Failed to add proposal"
                )
    }
}
