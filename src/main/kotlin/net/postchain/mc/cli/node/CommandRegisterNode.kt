package net.postchain.mc.cli.node

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.deprecated
import com.github.ajalt.clikt.parameters.options.flag
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
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.portOption
import net.postchain.mc.cli.requiredHostOption
import net.postchain.mc.cli.util.clusterUnitsOption
import net.postchain.mc.cli.util.extraStorageOption
import net.postchain.mc.cli.util.pubkeyOption
import net.postchain.mc.cli.util.requiredUrlOption
import net.postchain.mc.compatibility.ApiCompatV28.NodeCapabilityTypeV28
import net.postchain.mc.compatibility.ApiCompatV28.updateNodeCapabilityOperationV28
import net.postchain.mc.network.NodeVerifier
import java.io.IOException

class CommandRegisterNode : DCBaseCommand(
        name = "register",
        help = "Registers a node"
) {
    private val key by pubkeyOption("Node pubkey")

    private val host by requiredHostOption()

    private val port by portOption().required()

    private val apiUrl by requiredUrlOption("api url", "-a", "--api-url")

    private val territory by option("-t", "--territory", help = "ISO 3166-1 alpha-2 code").required().validate {
        require(it.length == 2)
    }

    private val clusterUnits by clusterUnitsOption().default(1)

    private val extraStorage by extraStorageOption().default(0)

    private val clusters by option(
            "-c",
            "--cluster",
            help = "Comma delimited list of clusters this node belongs to"
    ).split(",").default(emptyList())

    private val capability by option(help = "Node capability").enum<NodeCapabilityTypeV28>().multiple().deprecated()

    private val disableAccessChecks by option("--disable-access-checks", help = "Disable node and REST API access checks").flag()

    override fun runDC() {
        if (!disableAccessChecks) {
            val verifier = NodeVerifier(client.config, null)
            if (!verifier.verifyApi(apiUrl).responds) throw CliktError("Api url is not accessible for host")
            try {
                verifier.verifyHost(host, port)
            } catch (e: IOException) {
                throw CliktError("Node is not accessible: $e")
            }
        }
        val apiVersion = client.apiVersion()

        if (apiVersion < 15) {
            echo("Territory is not supported in API version $apiVersion and will be ignored")
        }
        if (capability.isNotEmpty() && apiVersion > 28) {
            echo("Node capability is not supported in API version $apiVersion and will be ignored")
        }

        client.transactionBuilder()
                .apply {
                    when {
                        apiVersion >= 24 -> {
                            registerNodeWithNodeDataOperation(clientProviderPubkey, RegisterNodeData(WrappedByteArray(key.data), host, port.toLong(), apiUrl, clusters, clusterUnits, territory, extraStorage))
                        }

                        apiVersion >= 15 -> {
                            registerNodeWithTerritoryAndUnitsOperation(clientProviderPubkey, key.data, host, port.toLong(), apiUrl, territory, clusterUnits, clusters)
                        }

                        apiVersion >= 3 -> registerNodeWithUnitsOperation(clientProviderPubkey, key.data, host, port.toLong(), apiUrl, clusters, clusterUnits)
                        else -> registerNodeOperation(clientProviderPubkey, key.data, host, port.toLong(), apiUrl, clusters)
                    }
                }
                .apply {
                    if (apiVersion <= 28 && capability.isNotEmpty()) capability.forEach { updateNodeCapabilityOperationV28(clientProviderPubkey, key.data, it, true) }
                }
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Node registered",
                        "Failed to register node"
                )
    }
}
