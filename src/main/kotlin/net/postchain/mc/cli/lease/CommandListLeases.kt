package net.postchain.mc.cli.lease

import com.chromia.cli.tools.ft.findAccountId
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.input.interactiveSelectList
import net.postchain.client.core.PostchainClient
import net.postchain.common.toHex
import net.postchain.economy.economy_chain.getLeasesByAccount
import net.postchain.mc.cli.ECBaseCommand
import net.postchain.mc.cli.accountIdOption
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.optionalEvmAddressOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcTable

class CommandListLeases : ECBaseCommand(
        name = "list",
        help = "List leases",
) {
    val maybeAccountId by accountIdOption()
    val maybeEvmAddress by optionalEvmAddressOption()

    val interactive by interactiveOption()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {
        val accountId = maybeAccountId ?: findAccountId(economyChainClient, maybeEvmAddress ?: throw UsageError("Need to specify either account id or EVM address"))
        val leases = economyChainClient.getLeasesByAccount(accountId)
        if (interactive && leases.isNotEmpty() && leases.size < terminal.size.height) {
            terminal.interactiveSelectList(leases.map {
                it.containerName
            }, "Select lease")?.let {
                showLeaseInfo(economyChainClient, it)
            }
        } else {
            echo(pmcTable(
                    "leases for account ${accountId.toHex()}",
                    listOf("Cluster", "Container", "Container Units (SCU)", "Extra storage (GiB)", "Expire Time (millis)", "Expired", "Auto Renewal", "Subnode image", "Subnode JAR extensions"),
                    leases.map { listOf(it.clusterName, it.containerName, it.containerUnits.toString(), it.extraStorageGib.toString(), it.expireTimeMillis.toString(), it.expired.toString(), it.autoRenew.toString(), it.subnodeImageName, it.subnodeJarExtensionNames.joinToString(", ")) },
                    idColumn = 1 to 64,
                    interactive = interactive
            ))
            if (interactive && leases.isNotEmpty()) {
                promptForIndex(leases)?.let {
                    showLeaseInfo(economyChainClient, leases[it].containerName)
                }
            }
        }
    }
}
