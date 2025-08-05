package net.postchain.mc.cli.cluster

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.long
import net.postchain.chain0.version.apiVersion
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.nameOrGenerateOption
import net.postchain.mc.compatibility.ApiCompatV28.requestClusterOperationV28

class CommandRequestCluster : DCBaseCommand(
        name = "request",
        help = "Request system creating a new cluster (is not supported after version 29)",
) {
    private val name by nameOrGenerateOption("Cluster name")

    private val size by option(help = "Size of cluster to be created").long().required()

    private val requireFull by option(help = "Fail if cluster is not full").flag("--do-not-require-full", default = true)

    override fun runDC() {
        val apiVersion = client.apiVersion()
        if (apiVersion >= 29) {
            throw CliktError("This operation is not supported after version 29 (version = $apiVersion)")
        } else {
            client.transactionBuilder()
                    .requestClusterOperationV28(clientProviderPubkey, name, size, requireFull)
                    .postAwaitConfirmation()
                    .printResult(
                            "Cluster $name was created",
                            "Could not create cluster"
                    )
        }
    }
}
