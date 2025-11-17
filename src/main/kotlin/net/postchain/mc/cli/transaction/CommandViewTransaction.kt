package net.postchain.mc.cli.transaction

import com.chromia.build.tools.multisignature.MultiSignatureTxData
import com.chromia.cli.base.formatter.json
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.inputStream
import net.postchain.common.toHex
import net.postchain.crypto.PubKey
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvArray
import net.postchain.gtv.GtvDictionary
import net.postchain.gtv.GtvNull
import net.postchain.gtx.Gtx
import net.postchain.mc.cli.PmcCommand

class CommandViewTransaction : PmcCommand(name = "view", help = "View a saved transaction") {

    val transactionStream by argument(name = "file", help = "transaction file, or - for STDIN")
            .inputStream()

    override fun run() {
        val txData = MultiSignatureTxData.decode(transactionStream.readAllBytes().toString(Charsets.UTF_8))
        val gtx = Gtx.decode(txData.transaction)
        echo(json(parseTransactionGtxForJson(gtx, txData.txRid)))
        if (terminal.terminalInfo.outputInteractive) {
            val missingSigners = (gtx.gtxBody.signers zip gtx.signatures).filter { it.second.isEmpty() }.map { PubKey(it.first) }
            if (missingSigners.isEmpty()) {
                echo("Transaction is fully signed and ready to be sent")
            } else {
                echo("Transaction needs to be signed by $missingSigners before it can be sent")
            }
        }
    }

    private fun parseTransactionGtxForJson(gtx: Gtx, txRid: ByteArray): Map<String, Any> = mapOf(
            "transactionRID" to txRid.toHex(),
            "blockchainRID" to gtx.gtxBody.blockchainRid.toHex(),
            "operations" to
                    gtx.gtxBody.operations.map { op ->
                        mapOf(
                                "operation" to op.opName,
                                "arguments" to op.args.map { arg -> argumentParser(arg) }

                        )
                    },
            "signers" to gtx.gtxBody.signers.map { signer -> signer.toHex() },
            "signatures" to gtx.signatures.map { signature -> signature.toHex() }
    )

    private fun argumentParser(arg: Gtv): Any = when (arg) {
        is GtvArray -> arg.array.map { argumentParser(it) }
        is GtvDictionary -> arg.asDict().mapValues { (_, v) -> argumentParser(v) }
        is GtvNull -> "null"
        else -> arg.getRawGtv().toString()
    }
}
