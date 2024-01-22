package net.postchain.mc.cli.node

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.deprecated
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.enum
import net.postchain.chain0.common.operations.registerNodeOperation
import net.postchain.chain0.common.operations.registerNodeWithNodeDataOperation
import net.postchain.chain0.common.operations.registerNodeWithTerritoryAndUnitsOperation
import net.postchain.chain0.common.operations.registerNodeWithUnitsOperation
import net.postchain.chain0.model.RegisterNodeData
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
import net.postchain.mc.network.NodeVerifier

class CommandRegisterNode : CliktCommand(
        name = "register",
        help = "Registers a node"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    private val key by pubkeyOption("Node pubkey")

    private val host by hostOption().required()

    private val port by portOption().required()

    private val apiUrl by option("-a", "--api-url", help = "api url").required()

    private val territory by option("-t", "--territory", help = "ISO 3166-1 alpha-2 code").validate {
        require(it.isNotBlank())
    }

    private val clusterUnits by clusterUnitsOption().default(1)

    private val extraStorage by extraStorageOption().default(0)

    private val clusters by option(
            "-c",
            "--cluster",
            help = "Comma delimited list of clusters this node belongs to"
    ).split(",").default(emptyList())

    private val capability by option(help = "Node capability").enum<NodeCapabilityTypeV28>().multiple().deprecated()

    override fun run() {
        val verifier = NodeVerifier(client.config, null)
        if (!verifier.verifyApi(apiUrl).responds) throw CliktError("Api url is not accessible for host")
        if (!verifier.verifyHost(host, port)) throw CliktError("Node is not accessible")
        val apiVersion = client.apiVersion()

        if (territory != null && apiVersion < 15) {
            echo("Territory is not supported in API version $apiVersion and will be ignored")
        }
        if (capability.isNotEmpty() && apiVersion > 28) {
            echo("Node capability is not supported in API version $apiVersion and will be ignored")
        }

        client.transactionBuilder()
                .apply {
                    when {
                        apiVersion >= 24 -> {
                            territory?.let {
                                registerNodeWithNodeDataOperation(client.pubkey, RegisterNodeData(WrappedByteArray(key.data), host, port.toLong(), apiUrl, clusters, clusterUnits, it, extraStorage))
                            } ?: throw CliktError("Territory must be specified")
                        }

                        apiVersion >= 15 -> {
                            territory?.let {
                                registerNodeWithTerritoryAndUnitsOperation(client.pubkey, key.data, host, port.toLong(), apiUrl, it, clusterUnits, clusters)
                            } ?: throw CliktError("Territory must be specified")
                        }

                        apiVersion >= 3 -> registerNodeWithUnitsOperation(client.pubkey, key.data, host, port.toLong(), apiUrl, clusters, clusterUnits)
                        else -> registerNodeOperation(client.pubkey, key.data, host, port.toLong(), apiUrl, clusters)
                    }
                }
                .apply {
                    if (apiVersion <= 28 && capability.isNotEmpty()) capability.forEach { updateNodeCapabilityOperationV28(client.pubkey, key.data, it, true) }
                }
                .postAwaitConfirmation()
                .printResult(
                        "Node registered",
                        "Failed to register node"
                )
    }
}
