package net.postchain.mc.cli.transaction

import com.chromia.build.tools.multisignature.MultiSignatureTxData
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.inputStream
import net.postchain.common.toHex
import net.postchain.crypto.PubKey
import net.postchain.gtx.Gtx
import net.postchain.gtx.GtxSignatureBuilder
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.SignatureData
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.pmcKeyConfigOption

class CommandSendTransaction : PmcCommand(name = "send", help = "Send a transaction") {

    val config by pmcKeyConfigOption()

    val transactionStream by argument(name = "file", help = "transaction file, or - for STDIN")
            .inputStream()

    val signatureFiles by option("--signature", "-sig", help = "signature file")
            .file(canBeFile = true, canBeDir = false, mustBeReadable = true)
            .multiple()

    override fun run() {
        val txData = MultiSignatureTxData.decode(transactionStream.readAllBytes().toString(Charsets.UTF_8))
        var gtx = Gtx.decode(txData.transaction)
        if (gtx.signatures.any { it.isEmpty() }) {
            val missingSigners = (gtx.gtxBody.signers zip gtx.signatures).filter { it.second.isEmpty() }.map { PubKey(it.first) }.toSet()
            val providedSignatures = signatureFiles
                    .map { SignatureData.decode(it.readText()) }
                    .filter {
                        if (!it.txRid.contentEquals(txData.txRid))
                            throw CliktError("Got signature for unexpected transaction ${it.txRid.toHex()}, expected ${txData.txRid.toHex()}")
                        else true
                    }
                    .filter { missingSigners.contains(PubKey(it.signature.subjectID)) }
                    .map { it.signature }

            val signBuilder = GtxSignatureBuilder(gtx.gtxBody, txData.txRid, config.clientConfig.cryptoSystem, check = false)
            signBuilder.addSignatures(gtx.signatures)
            providedSignatures.forEach {
                signBuilder.sign(it)
            }
            gtx = signBuilder.buildGtx()

            val stillMissingSigners = (gtx.gtxBody.signers zip gtx.signatures).filter { it.second.isEmpty() }.map { PubKey(it.first) }.toSet()
            if (stillMissingSigners.isNotEmpty()) {
                throw CliktError("Transaction needs to be signed by $stillMissingSigners before it can be sent")
            }
        }

        val client = if (config.lookupNodes)
            config.chromiaClient.getSystemChainClient(gtx.gtxBody.blockchainRid, addNop = false)
        else
            config.chromiaClient.getSystemChainClientForForwardingReplica(gtx.gtxBody.blockchainRid, addNop = false)

        client.postTransactionAwaitConfirmation(gtx, txListener()).printResult(
                "Transaction sent successfully",
                "Failed to send transaction"
        )
    }
}
