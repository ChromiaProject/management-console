package net.postchain.mc.cli.economy

import com.chromia.cli.tools.ft.fetchEvmSignatures
import com.chromia.directory1.lib.ft4.external.auth.evmSignaturesOperation
import com.chromia.directory1.lib.ft4.external.auth.ftAuthOperation
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.client.core.PostchainClient
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.economy.economy_chain.getProviderAccountId
import net.postchain.economy.lib.ft4.core.accounts.AuthDescriptor
import net.postchain.economy.lib.ft4.core.accounts.AuthType
import net.postchain.economy.lib.ft4.external.accounts.UPDATE_MAIN_AUTH_DESCRIPTOR
import net.postchain.economy.lib.ft4.external.accounts.getAccountMainAuthDescriptor
import net.postchain.economy.lib.ft4.external.accounts.updateMainAuthDescriptorOperation
import net.postchain.economy.lib.hbridge.LINK_EVM_EOA_ACCOUNT
import net.postchain.economy.lib.hbridge.linkEvmEoaAccountOperation
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.mc.cli.base.printResult

class CommandAuthDescriptorEvmSwap : ECBaseCommand(
        name = "auth-descriptor-evm-swap",
        help = "This command will swap the main auth descriptor signer of an ft4 provider account with the provided " +
                "EVM address and set the auth flags to both T (Transfer) and A (Account). " +
                "It will also link the account to an EOA (external owned account) using the EVM address"
) {

    val accountIdOption by option("--account-id", help = "Account id of the account to be updated.")
            .convert { it.hexStringToByteArray() }.validate { require(it.isNotEmpty()) { "Account id cannot be empty." } }

    val evmAddress by option(help = "EVM address", metavar = "address")
            .convert {
                (if (it.startsWith("0x")) it.drop(2) else it).hexStringToByteArray()
            }
            .required()
            .validate { require(it.size == 20) { "EVM address must be 20 bytes" } }

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        val providerPubkey = economyChainClient.config.signers.firstOrNull()?.pubKey?.data
                ?: throw CliktError("No provider")
        val accountId = accountIdOption
                ?: economyChainClient.getProviderAccountId(providerPubkey).also { if (it != null) echo("Using account id ${it.toHex()}") }
                ?: throw CliktError("No account id found for provider")

        val accountMainAuthDescriptor = economyChainClient.getAccountMainAuthDescriptor(accountId)

        val authDescriptor = gtv(
                gtv(AuthType.S.ordinal.toLong()),
                gtv(gtv(gtv("A"), gtv("T")), gtv(evmAddress)),
                GtvNull)
        val (linkEvmEoaAccountSignature, updateMainAuthDescriptorSignature) = fetchEvmSignatures(
                economyChainClient,
                listOf(
                        LINK_EVM_EOA_ACCOUNT to listOf(gtv(evmAddress)),
                        UPDATE_MAIN_AUTH_DESCRIPTOR to listOf(authDescriptor)
                ),
                evmAddress, accountId, accountMainAuthDescriptor.id.data)
        echo("Signing done, posting transaction...")
        economyChainClient.transactionBuilder()
                .evmSignaturesOperation(listOf(evmAddress), listOf(linkEvmEoaAccountSignature))
                .ftAuthOperation(accountId, accountMainAuthDescriptor.id.data)
                .linkEvmEoaAccountOperation(evmAddress)
                .evmSignaturesOperation(listOf(evmAddress), listOf(updateMainAuthDescriptorSignature))
                .ftAuthOperation(accountId, accountMainAuthDescriptor.id.data)
                .updateMainAuthDescriptorOperation(AuthDescriptor(AuthType.S, listOf(gtv(gtv("A"), gtv("T")), gtv(evmAddress)), GtvNull))
                .postAwaitConfirmation()
                .printResult(
                        "Link EVM account to EOA account and update auth description signer with EVM address: 0x${evmAddress.toHex()}",
                        "Failed to link and update auth descriptor signer to EVM address. "
                )
    }
}
