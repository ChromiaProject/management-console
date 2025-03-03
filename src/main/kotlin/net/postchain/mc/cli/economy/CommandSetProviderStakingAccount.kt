package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.client.core.PostchainClient
import net.postchain.common.toHex
import net.postchain.crypto.PubKey
import net.postchain.economy.economy_chain.setProviderStakingAccountOperation
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.merkle.GtvMerkleHashCalculatorV2
import net.postchain.gtv.merkleHash
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.pubkeyOption
import net.postchain.mc.cli.util.pubkeyValidator
import org.apache.commons.codec.digest.DigestUtils.sha256

class CommandSetProviderStakingAccount : ECBaseCommand(
        name = "set-provider-staking-account",
        help = """
            Set account to be used for providers staking. Existing staking is transferred to new account.
            
            Transaction must be signed by provider, current and new staking account key.
        """.trimIndent(),
        requiresECVersion = 40,
) {
    private val providerPubkey by pubkeyOption("Provider public key")
    private val accountPubkey by option("--account-pk", help = "Public key of new staking account")
            .convert { PubKey(it) }
            .required()
            .validate(pubkeyValidator())

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        val hashCalculator = GtvMerkleHashCalculatorV2(::sha256)

        economyChainClient
                .transactionBuilder()
                .setProviderStakingAccountOperation(providerPubkey.data, accountPubkey.data)
                .postAwaitConfirmation()
                .printResult(
                        "Staking account set to ${gtv(accountPubkey.data).merkleHash(hashCalculator).toHex()}",
                        "Failed to set staking account"
                )
    }
}