package net.postchain.mc.cli.cluster

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_cluster.proposeClusterProviderOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.pubkeyOption


class CommandProposeClusterProvider : PmcCommand(
        name = "provider",
        help = """
            Proposes an update of a cluster's providers
            
            To remove provider from cluster set --add to false. 
            Cluster governance voter set has authority to update a cluster's providers
        """.trimIndent()
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val provider by pubkeyOption()

    private val clusterName by option(
            "-c", "--cluster",
            help = "Name of existing cluster to update"
    ).required()

    private val add by option("-a", "--add", help = "Add or remove provider pubkey from cluster")
            .flag("-r", "--remove", default = true)

    private val description by proposalDescriptionOption {
        if (add) "Add cluster provider $provider to the cluster $clusterName"
        else "Remove cluster provider $provider from the cluster $clusterName"
    }

    override fun run() {
        client.transactionBuilder()
                .proposeClusterProviderOperation(client.pubkey, clusterName, provider.data, add, description)
                .postAwaitConfirmation()
                .printResult(
                        "Cluster $clusterName providers update proposed",
                        "Failed proposing cluster $clusterName providers update"
                )
    }
}