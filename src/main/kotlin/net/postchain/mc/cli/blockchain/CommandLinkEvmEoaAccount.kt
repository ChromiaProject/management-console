package net.postchain.mc.cli.blockchain

import com.chromia.cli.tools.ft.addEvmAuthOperation
import com.chromia.cli.tools.ft.addEvmSignaturesOperation
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.common.toHex
import net.postchain.economy.lib.ft4.external.accounts.getAccountMainAuthDescriptor
import net.postchain.economy.lib.hbridge.LINK_EVM_EOA_ACCOUNT
import net.postchain.economy.lib.hbridge.linkEvmEoaAccountOperation
import net.postchain.gtv.GtvFactory
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.accountIdOption
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.evmAddressOption
import net.postchain.mc.cli.util.pmcKeyConfigOption

class CommandLinkEvmEoaAccount : PmcCommand(
        name = "link-evm-eoa-account",
        help = "This command links an EVM Externally Owned Account (EOA) to a Chromia account on the specified blockchain"
) {

    val config by pmcKeyConfigOption()
    val client get() = config.txClient

    val blockchainRid by blockchainRidOption().required()
    val accountIdOption by accountIdOption(help = "Account id of the account to be updated.")
    val evmAddress by evmAddressOption()

    override fun run() {
        val accountId = accountIdOption ?: throw CliktError("No account id found")
        val chainClient = config.chromiaClient.getClient(blockchainRid, addNop = true)
        val accountMainAuthDescriptor = chainClient.getAccountMainAuthDescriptor(accountId)

        chainClient.transactionBuilder().also {
            echo("Adding EVM signatures ...")
            addEvmSignaturesOperation(
                    chainClient,
                    it,
                    LINK_EVM_EOA_ACCOUNT,
                    listOf(GtvFactory.gtv(evmAddress)),
                    evmAddress,
                    accountId,
                    accountMainAuthDescriptor.id.data
            )

            echo("Adding EVM auth operation ...")
            addEvmAuthOperation(
                    chainClient,
                    it,
                    LINK_EVM_EOA_ACCOUNT,
                    listOf(GtvFactory.gtv(evmAddress)),
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