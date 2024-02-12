package net.postchain.mc.cli.economy

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.client.core.PostchainClient
import net.postchain.economy.economy_chain.changeClusterTagOperation
import net.postchain.mc.cli.base.printResult

class CommandChangeClusterTag : ECBaseCommand(
        name = "change-cluster-tag",
        help = "Change the tag of a cluster"
) {
    private val clusterName by option("-cn", "--cluster-name", help = "Name of the cluster").required()
    private val tagName by option("-tn", "--tag-name", help = "Name of the tag").required()

    override fun runEC(client: PostchainClient, economyChainClient: PostchainClient) {

        economyChainClient.transactionBuilder()
                .changeClusterTagOperation(clusterName, tagName)
                .postAwaitConfirmation()
                .printResult(
                        "Proposal created for changing tag of cluster $clusterName to $tagName",
                        "Failed to change tag of cluster"
                )
    }
}