package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.economy.economy_chain.changeClusterTagOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.pmcConfigOption

class CommandChangeClusterTag : CliktCommand(
        name = "change-cluster-tag",
        help = "Change the tag of a cluster"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val clusterName by option("-cn", "--cluster-name", help = "Name of the cluster").required()
    private val tagName by option("-tn", "--tag-name", help = "Name of the tag").required()

    override fun run() {
        val economyChainClient = getEconomyChainClient(client, config.config)

        economyChainClient.transactionBuilder()
                .changeClusterTagOperation(clusterName, tagName)
                .postAwaitConfirmation()
                .printResult(
                        "Tag of cluster $clusterName was changed to $tagName",
                        "Failed to change tag of cluster"
                )
    }
}