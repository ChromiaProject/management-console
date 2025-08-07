package net.postchain.mc.cli.node

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import net.postchain.chain0.common.operations.registerNodeWithNodeDataOperation
import net.postchain.chain0.model.RegisterNodeData
import net.postchain.common.types.WrappedByteArray
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.defaultHostOption
import net.postchain.mc.cli.portOption
import net.postchain.mc.cli.util.defaultUrlOption
import net.postchain.mc.cli.util.pubkeyOption

class CommandRegisterReplicaNode : DCBaseCommand(
        name = "register-replica",
        help = "Registers a replica node",
        requiresVersion = 57,
) {
    private val key by pubkeyOption("Node pubkey")

    private val host by defaultHostOption("")

    private val port by portOption().default(0)

    private val apiUrl by defaultUrlOption("", "api url", "-a", "--api-url")

    private val territory by option("-t", "--territory", help = "ISO 3166-1 alpha-2 code")
            .default("")
            .validate {
                require(it.length == 2 || it.isEmpty())
            }

    override fun runDC() {
        client.transactionBuilder()
                .apply {
                    registerNodeWithNodeDataOperation(clientProviderPubkey, RegisterNodeData(WrappedByteArray(key.data), host, port.toLong(), apiUrl, emptyList(), 1, territory, 0))
                }
                .postAwaitConfirmation(txListener())
                .printResult(
                        "Replica node registered",
                        "Failed to register replica node"
                )
    }
}
