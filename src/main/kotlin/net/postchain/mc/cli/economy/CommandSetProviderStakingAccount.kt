package net.postchain.mc.cli.economy

import com.chromia.cli.tools.ft.addEvmAuthOperation
import com.chromia.cli.tools.ft.findFtAccountIdWithAuthDescriptorId
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.client.core.PostchainClient
import net.postchain.common.toHex
import net.postchain.crypto.PubKey
import net.postchain.economy.economy_chain.SET_PROVIDER_STAKING_ACCOUNT_FT4
import net.postchain.economy.economy_chain.setProviderStakingAccountFt4Operation
import net.postchain.economy.economy_chain.setProviderStakingAccountOperation
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.merkle.GtvMerkleHashCalculatorV2
import net.postchain.gtv.merkleHash
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.accountIdOption
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.optionalEvmAddressOption
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
            .validate(pubkeyValidator())
    private val evmAddress by optionalEvmAddressOption(help = "EVM address of new staking account")
    private val accountIdOption by accountIdOption(help = "Account id of new staking account")

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        if (accountPubkey != null && evmAddress != null) throw UsageError("Can't use both --account-pk and --evm-address")
        if (accountPubkey != null && accountIdOption != null) throw UsageError("Can't use both --account-pk and --account-id")
        if (accountPubkey == null && evmAddress == null) throw UsageError("Either --account-pk or --evm-address must be provided")

        val hashCalculator = GtvMerkleHashCalculatorV2(::sha256)

        if (ecVersion.version >= 55 && evmAddress != null) {
            val (accountId, authDescriptorId) =
                    findFtAccountIdWithAuthDescriptorId(economyChainClient, accountIdOption, evmAddress!!, SET_PROVIDER_STAKING_ACCOUNT_FT4, null)

            economyChainClient.transactionBuilder().also {
                addEvmAuthOperation(
                        economyChainClient, it,
                        SET_PROVIDER_STAKING_ACCOUNT_FT4,
                        listOf(gtv(providerPubkey.data)),
                        evmAddress!!, accountId, authDescriptorId)
                echo("Signing done, posting transaction...", err = true)
            }
                    .setProviderStakingAccountFt4Operation(providerPubkey.data)
                    .postAwaitConfirmation(txListener())
                    .printResult(
                            "Staking account set to ${accountId.toHex()}",
                            "Failed to set staking account"
                    )
        } else if (accountPubkey != null) {
            economyChainClient
                    .transactionBuilder()
                    .setProviderStakingAccountOperation(providerPubkey.data, accountPubkey!!.data)
                    .postAwaitConfirmation(txListener())
                    .printResult(
                            "Staking account set to ${gtv(accountPubkey!!.data).merkleHash(hashCalculator).toHex()}",
                            "Failed to set staking account"
                    )
        } else {
            throw CliktError("--account-pk required for EC version < 55 (is ${ecVersion.version})")
        }
    }
}