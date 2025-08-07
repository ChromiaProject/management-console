package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.updateProviderStakingRewardsShareOperation
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.pubkeyOption
import java.math.BigDecimal

class CommandUpdateProviderStakingRewardShare : ECBaseCommand(
        name = "update-provider-staking-reward-share",
        help = """
            Update staking reward share for a node provider.
            
            Transaction must be signed by provider account key.
        """.trimIndent(),
        requiresECVersion = 61,
) {
    private val providerPubkey by pubkeyOption("Provider public key")
    private val stakingRewardShare by option("--staking-reward-share", help = "Staking reward share (0-100 %)")
            .convert { BigDecimal(it) / BigDecimal(100) }
            .required()
            .validate {
                if (it <= BigDecimal.ZERO || it >= BigDecimal.ONE)
                    throw UsageError("Staking reward share must be between 0 and 100")
            }

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        economyChainClient
                .transactionBuilder()
                .updateProviderStakingRewardsShareOperation(providerPubkey.data, stakingRewardShare)
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Staking reward share scheduled to be updated",
                        "Failed to update staking reward share"
                )
    }
}
