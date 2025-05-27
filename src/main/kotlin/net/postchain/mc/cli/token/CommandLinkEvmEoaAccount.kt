package net.postchain.mc.cli.token

import com.chromia.cli.tools.ft.addEvmAuthOperation
import com.chromia.cli.tools.ft.addEvmSignaturesOperation
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.token_chain_in_directory_chain.getTokenChainRid
import net.postchain.client.core.PostchainClient
import net.postchain.common.BlockchainRid
import net.postchain.common.toHex
import net.postchain.economy.lib.ft4.external.accounts.getAccountMainAuthDescriptor
import net.postchain.economy.lib.hbridge.LINK_EVM_EOA_ACCOUNT
import net.postchain.economy.lib.hbridge.linkEvmEoaAccountOperation
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.accountIdOption
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.economy.DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION
import net.postchain.mc.cli.economy.DIRECTORY_CHAIN_TOKEN_CHAIN_VERSION
import net.postchain.mc.cli.evmAddressOption
import net.postchain.mc.cli.util.PmcClientConfigOption
import net.postchain.mc.cli.util.pmcKeyConfigOption
import net.postchain.mc.network.Version

class CommandLinkEvmEoaAccount : PmcCommand(
        name = "link-evm-eoa-account",
        help = "This command links an EVM Externally Owned Account (EOA) to a Chromia account on the Token Chain."
) {

    val config by pmcKeyConfigOption()
    val client get() = config.txClient

    val accountIdOption by accountIdOption(help = "Account id of the account to be updated.")
    val evmAddress by evmAddressOption()

    override fun run() {
        val tokenChainClient = getTokenChainClient(config)
        val accountId = accountIdOption ?: throw CliktError("No account id found")
        val accountMainAuthDescriptor = tokenChainClient.getAccountMainAuthDescriptor(accountId)
//        echo("Signing done, posting transaction...")

        tokenChainClient.transactionBuilder().also {
            echo("Adding EVM signatures ...")
            addEvmSignaturesOperation(
                    tokenChainClient,
                    it,
                    LINK_EVM_EOA_ACCOUNT,
                    listOf(gtv(evmAddress)),
                    evmAddress,
                    accountId,
                    accountMainAuthDescriptor.id.data
            )

            echo("Adding EVM auth operation ...")
            addEvmAuthOperation(
                    tokenChainClient,
                    it,
                    LINK_EVM_EOA_ACCOUNT,
                    listOf(gtv(evmAddress)),
                    evmAddress,
                    accountId,
                    accountMainAuthDescriptor.id.data
            )
            echo("Linking EVM account to Chromia account ...")
        }
                .linkEvmEoaAccountOperation(evmAddress)
                .postAwaitConfirmation()
                .printResult(
                        "Link EVM account to Chromia account and update auth description signer with EVM address: 0x${evmAddress.toHex()}",
                        "Failed to link and update auth descriptor signer to EVM address. "
                )
    }
}

fun getTokenChainClient(config: PmcClientConfigOption): PostchainClient {

    val version = Version(config.client).version
    if (version < DIRECTORY_CHAIN_TOKEN_CHAIN_VERSION) {
        throw CliktError("Token chain requires directory chain version $DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION, found version $version")
    }

    val tokenChainBrid = config.client.getTokenChainRid()

    return if (config.lookupNodes)
        config.chromiaClient.getSystemChainClient(BlockchainRid(tokenChainBrid), addNop = true)
    else
        config.chromiaClient.getSystemChainClientForForwardingReplica(BlockchainRid(tokenChainBrid), addNop = true)
}