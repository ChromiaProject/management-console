package net.postchain.mc.cli.node

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.deprecated
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.enum
import net.postchain.chain0.common.operations.addNodeToClusterOperation
import net.postchain.chain0.common.operations.updateNodeOperation
import net.postchain.chain0.common.operations.updateNodeWithNodeDataOperation
import net.postchain.chain0.common.operations.updateNodeWithTerritoryAndUnitsOperation
import net.postchain.chain0.common.operations.updateNodeWithUnitsOperation
import net.postchain.chain0.model.UpdateNodeData
import net.postchain.chain0.version.apiVersion
import net.postchain.common.types.WrappedByteArray
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.hostOption
import net.postchain.mc.cli.portOption
import net.postchain.mc.cli.util.clusterUnitsOption
import net.postchain.mc.cli.util.extraStorageOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pubkeyOption
import net.postchain.mc.compatibility.ApiCompatV28.NodeCapabilityTypeV28
import net.postchain.mc.compatibility.ApiCompatV28.updateNodeCapabilityOperationV28

class CommandUpdateNode : CliktCommand(
        name = "update",
        help = "Update node information"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val key by pubkeyOption()

    private val host by hostOption()

    private val port by portOption()

    private val apiUrl by option("-a", "--api-url", help = "api url")

    private val territory by option("-t", "--territory", help = "ISO 3166-1 alpha-2 code").validate {
        require(it.isNotBlank())
    }

    private val clusterUnits by clusterUnitsOption()

    private val extraStorage by extraStorageOption()

    private val clusterName by option(
            "-c",
            "--cluster",
            help = "comma delimited list of clusters to add this node to"
    ).split(",")

    private val addCapability by option(help = "Node capability").enum<NodeCapabilityTypeV28>().deprecated()
    private val removeCapability by option(help = "Node capability").enum<NodeCapabilityTypeV28>().deprecated()

    override fun run() {
        if (host == null && port == null && apiUrl == null && clusterUnits == null && clusterName == null && addCapability == null && removeCapability == null && territory == null && extraStorage == null) {
            echo("No properties to update. At least one node's property should be specified")
            return
        }

        val apiVersion = client.apiVersion()
        if (territory != null && apiVersion < 15) {
            echo("Territory is not supported in API version $apiVersion and will be ignored")
        }
        if (extraStorage != null && apiVersion < 24) {
            echo("Extra Storage is not supported in API version $apiVersion and will be ignored")
        }
        if ((addCapability != null || removeCapability != null) && apiVersion > 28) {
            echo("Node capability is not supported in API version $apiVersion and will be ignored")
        }

        val provider = client.config.pubkey().data
        client.transactionBuilder()
                .apply {
                    when {
                        apiVersion >= 24 -> {
                            if (host != null || port != null || apiUrl != null || clusterUnits != null || territory != null || extraStorage != null) {
                                updateNodeWithNodeDataOperation(provider, UpdateNodeData(WrappedByteArray(key.data), host, port?.toLong(), apiUrl, clusterUnits, territory, extraStorage))
                            }
                        }

                        apiVersion >= 15 -> {
                            if (host != null || port != null || apiUrl != null || clusterUnits != null || territory != null) {
                                updateNodeWithTerritoryAndUnitsOperation(provider, key.data, host, port?.toLong(), apiUrl, territory, clusterUnits)
                            }
                        }

                        apiVersion >= 3 -> {
                            if (host != null || port != null || apiUrl != null || clusterUnits != null) {
                                updateNodeWithUnitsOperation(provider, key.data, host, port?.toLong(), apiUrl, clusterUnits)
                            }
                        }

                        else -> {
                            if (host != null || port != null || apiUrl != null) {
                                updateNodeOperation(provider, key.data, host, port?.toLong(), apiUrl)
                            }
                        }
                    }
                    clusterName?.forEach { addNodeToClusterOperation(provider, key.data, it) }

                    if (apiVersion <= 28) {
                        addCapability?.let { updateNodeCapabilityOperationV28(provider, key.data, it, true) }
                        removeCapability?.let { updateNodeCapabilityOperationV28(provider, key.data, it, false) }
                    }
                }
                .postAwaitConfirmation()
                .printResult("Node information was updated", "Node information update failed")
    }
}
