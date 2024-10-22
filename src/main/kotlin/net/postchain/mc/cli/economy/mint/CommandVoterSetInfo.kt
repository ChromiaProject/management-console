package net.postchain.mc.cli.economy.mint

import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.getChromiaFoundationVoterSetInfo
import net.postchain.mc.cli.economy.ECBaseCommand
import net.postchain.mc.cli.economy.ECONOMY_CHAIN_MINTING_VERSION
import net.postchain.mc.cli.util.pmcTable
import net.postchain.mc.cli.votingupdates.formatThreshold

class CommandVoterSetInfo : ECBaseCommand(
        name = "voter-set-info",
        help = "Show information of voter set",
        requiresECVersion = ECONOMY_CHAIN_MINTING_VERSION,
        printHelpOnEmptyArgs = false
) {
    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        val voterSet = economyChainClient.getChromiaFoundationVoterSetInfo()
        echo(pmcTable {
            body {
                row("Voter set", voterSet.name)
                row("Governed by", voterSet.governor)
                row("Threshold", formatThreshold(voterSet.threshold))
                voterSet.members.forEachIndexed { index, bytes -> row("Member $index", bytes.toHex()) }
            }
        })
    }
}
