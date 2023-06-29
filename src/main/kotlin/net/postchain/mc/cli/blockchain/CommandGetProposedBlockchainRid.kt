package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.proposal_blockchain.findBlockchainRid
import net.postchain.client.exception.ClientError
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.mc.cli.util.clientOption
import net.postchain.mc.network.requireApiVersion

class CommandGetProposedBlockchainRid : CliktCommand(
        name = "get-proposed-blockchain-rid",
        help = "Get proposed blockchain rid"
) {
    private val client by clientOption()

    private val txRid by option("-tx", "--tx-rid", help = "Transaction RID")
            .convert { it.hexStringToByteArray() }.required()

    private val quiet by option("-q", "--quiet", help = "Print only blockchain RID if succeeds").flag()

    override fun run() {
        client.requireApiVersion(8)

        val maybeBcRid: ByteArray? = try {
            client.findBlockchainRid(txRid)
        } catch (e: ClientError) {
            throw CliktError(e.message)
        }
        if (maybeBcRid != null) {
            echo(if (quiet) maybeBcRid.toHex() else "bc-rid: ${maybeBcRid.toHex()}")
        } else {
            throw CliktError("Blockchain proposal not approved")
        }
    }
}
