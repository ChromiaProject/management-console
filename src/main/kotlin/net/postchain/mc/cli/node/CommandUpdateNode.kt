package net.postchain.mc.cli.node

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.operations.addNodeToClusterOperation
import net.postchain.chain0.common.operations.updateNodeWithNodeDataOperation
import net.postchain.chain0.model.UpdateNodeData
import net.postchain.common.types.WrappedByteArray
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.hostOption
import net.postchain.mc.cli.portOption
import net.postchain.mc.cli.util.clusterUnitsOption
import net.postchain.mc.cli.util.extraStorageOption
import net.postchain.mc.cli.util.pubkeyOption
import net.postchain.mc.cli.util.urlOption

class CommandUpdateNode : DCBaseCommand(
        name = "update",
        help = "Update node information"
) {
    private val key by pubkeyOption()

    private val host by hostOption()

    private val port by portOption()

    private val apiUrl by urlOption("api url", "-a", "--api-url")

    private val territory by option("-t", "--territory", help = "ISO 3166-1 alpha-2 code").validate {
        require(it.length == 2)
    }

    private val clusterUnits by clusterUnitsOption()

    private val extraStorage by extraStorageOption()

    private val clusterName by option(
            "-c",
            "--cluster",
            help = "Comma delimited list of clusters to add this node to"
    ).split(",")

    override fun runDC() {
        if (host == null && port == null && apiUrl == null && clusterUnits == null && clusterName == null && territory == null && extraStorage == null) {
            throw CliktError("No properties to update. At least one node's property should be specified")
        }

        if (territory != null && dcVersion < 15) {
            echo("Territory is not supported in API version $dcVersion and will be ignored")
        }
        if (extraStorage != null && dcVersion < 24) {
            echo("Extra Storage is not supported in API version $dcVersion and will be ignored")
        }

        val provider = clientProviderPubkey
        transactionBuilder()
                .apply {
                    if (host != null || port != null || apiUrl != null || clusterUnits != null || territory != null || extraStorage != null) {
                        updateNodeWithNodeDataOperation(provider, UpdateNodeData(WrappedByteArray(key.data), host, port?.toLong(), apiUrl, clusterUnits, territory, extraStorage))
                    }
                    clusterName?.forEach { addNodeToClusterOperation(provider, key.data, it) }
                }
                .postOrSave()
                .printResult("Node information was updated", "Node information update failed")
    }
}
