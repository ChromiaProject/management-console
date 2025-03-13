package net.postchain.mc.cli.node

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.operations.replaceNodeOperation
import net.postchain.chain0.common.operations.replaceNodeWithNodeDataAndKeepOldNodeAsReplicaOperation
import net.postchain.chain0.common.operations.replaceNodeWithNodeDataOperation
import net.postchain.chain0.common.operations.replaceNodeWithUnitsAndTerritoryOperation
import net.postchain.chain0.common.operations.replaceNodeWithUnitsOperation
import net.postchain.chain0.model.ReplaceNodeData
import net.postchain.common.types.WrappedByteArray
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.hostOption
import net.postchain.mc.cli.portOption
import net.postchain.mc.cli.util.clusterUnitsOption
import net.postchain.mc.cli.util.extraStorageOption
import net.postchain.mc.cli.util.urlOption


class CommandReplaceNode : DCBaseCommand(
        name = "replace",
        help = """
        Replace a node with a new one (used to rotate keypairs)
        
        Add the keys to the nodes to the client configuration as comma-delimited list:
        pubkey=<key>,<old-node-key>,<new-node-key>
        privkey=<key>,<old-node-key>,<new-node-key>
    """.trimIndent()
) {
    private val old by option("--old-key", help = "Public key of the node to replace").convert { PubKey(it) }.required()
    private val new by option("--new-key", help = "Public key of the new node").convert { PubKey(it) }.required()

    private val host by hostOption()

    private val port by portOption()

    private val apiUrl by urlOption("api url", "-a", "--api-url")

    private val territory by option("-t", "--territory", help = "ISO 3166-1 alpha-2 code").validate {
        require(it.isNotBlank())
    }

    private val clusterUnits by clusterUnitsOption()

    private val extraStorage by extraStorageOption()

    private val keepOldNodeAsReplica by option("--keep-as-replica", help = "Keep replaced node as a replica node").flag()

    override fun runDC() {
        if (territory != null && dcVersion < 15) {
            echo("Territory is not supported in API version $dcVersion and will be ignored")
        }

        client.transactionBuilder()
                .apply {
                    if (keepOldNodeAsReplica) {
                        if (dcVersion < 73) {
                            throw CliktError("Keeping node as replica '--keep-as-replica' is not supported by network")
                        }
                        replaceNodeWithNodeDataAndKeepOldNodeAsReplicaOperation(clientProviderPubkey, ReplaceNodeData(WrappedByteArray(old.data), WrappedByteArray(new.data), host, port?.toLong(), apiUrl, clusterUnits, territory, extraStorage))
                    } else {
                        when {
                            dcVersion >= 24 -> replaceNodeWithNodeDataOperation(clientProviderPubkey, ReplaceNodeData(WrappedByteArray(old.data), WrappedByteArray(new.data), host, port?.toLong(), apiUrl, clusterUnits, territory, extraStorage))
                            dcVersion >= 15 -> replaceNodeWithUnitsAndTerritoryOperation(clientProviderPubkey, old.data, new.data, host, port?.toLong(), apiUrl, territory, clusterUnits ?: 1)
                            dcVersion >= 3 -> replaceNodeWithUnitsOperation(clientProviderPubkey, old.data, new.data, host, port?.toLong(), apiUrl, clusterUnits ?: 1)
                            else -> replaceNodeOperation(clientProviderPubkey, old.data, new.data, host, port?.toLong(), apiUrl)
                        }
                    }
                }
                .postAwaitConfirmation()
                .printResult(
                        "Node has been replaced",
                        "Failed to replace node"
                )
    }
}
