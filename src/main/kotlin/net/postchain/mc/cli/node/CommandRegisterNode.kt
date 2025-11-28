package net.postchain.mc.cli.node

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.operations.registerNodeWithNodeDataOperation
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

        transactionBuilder()
                .registerNodeWithNodeDataOperation(clientProviderPubkey, RegisterNodeData(WrappedByteArray(key.data), host, port.toLong(), apiUrl, clusters, clusterUnits, territory, extraStorage))
                .postOrSave()
                .printResult(
                        "Node registered",
                        "Failed to register node"
                )
    }
}
