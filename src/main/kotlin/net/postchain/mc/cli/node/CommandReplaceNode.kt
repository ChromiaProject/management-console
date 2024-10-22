package net.postchain.mc.cli.node

import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.operations.replaceNodeOperation
import net.postchain.chain0.common.operations.replaceNodeWithNodeDataOperation
import net.postchain.chain0.common.operations.replaceNodeWithUnitsAndTerritoryOperation
import net.postchain.chain0.common.operations.replaceNodeWithUnitsOperation
import net.postchain.chain0.model.ReplaceNodeData
import net.postchain.common.types.WrappedByteArray
import net.postchain.crypto.PubKey
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
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

    private val clusterUnits by clusterUnitsOption().default(1)

    private val extraStorage by extraStorageOption().default(0)

    override fun runDC() {
        if (territory != null && dcVersion < 15) {
            echo("Territory is not supported in API version $dcVersion and will be ignored")
        }

        client.transactionBuilder()
                .apply {
                    when {
                        dcVersion >= 24 -> replaceNodeWithNodeDataOperation(clientProviderPubkey, ReplaceNodeData(WrappedByteArray(old.data), WrappedByteArray(new.data), host, port?.toLong(), apiUrl, clusterUnits, territory, extraStorage))
                        dcVersion >= 15 -> replaceNodeWithUnitsAndTerritoryOperation(client.config.pubkey().data, old.data, new.data, host, port?.toLong(), apiUrl, territory, clusterUnits)
                        dcVersion >= 3 -> replaceNodeWithUnitsOperation(client.config.pubkey().data, old.data, new.data, host, port?.toLong(), apiUrl, clusterUnits)
                        else -> replaceNodeOperation(client.config.pubkey().data, old.data, new.data, host, port?.toLong(), apiUrl)
                    }
                }
                .postAwaitConfirmation()
                .printResult(
                        "Node has been replaced",
                        "Failed to replace node"
                )
    }
}
