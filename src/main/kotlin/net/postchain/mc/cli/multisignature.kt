package net.postchain.mc.cli

import com.chromia.build.tools.multisignature.MultiSignatureTxData
import com.github.ajalt.clikt.core.CoreCliktCommand
import mu.KLogging
import net.postchain.common.data.Hash
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.crypto.PubKey
import net.postchain.crypto.Signature
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvDecoder.decodeGtv
import net.postchain.gtv.GtvEncoder.encodeGtv
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.FromGtv
import net.postchain.gtv.mapper.ToGtv
import net.postchain.gtx.Gtx
import java.io.File

const val DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_VERSION = 65L
const val DIRECTORY_CHAIN_PROVIDER_MULTI_KEY_AND_THRESHOLD_VERSION = 103L

fun CoreCliktCommand.saveTransaction(outputFolder: File, txRid: Hash, gtx: Gtx) {
    val missingSigners = (gtx.gtxBody.signers zip gtx.signatures).filter { it.second.isEmpty() }.map { PubKey(it.first) }
    val fileName = if (missingSigners.isNotEmpty()) {
        "transaction_${txRid.toHex()}_${missingSigners.size}_signatures_missing.tx"
    } else {
        "transaction_${txRid.toHex()}_fully_signed.tx"
    }
    val file = outputFolder.resolve(fileName)
    file.writeText(MultiSignatureTxData(gtx.encode(), txRid).encode())
    echo("Transaction ${txRid.toHex()} is written as hex to file: ${file.absolutePath}")
    if (missingSigners.isNotEmpty()) {
        echo("Requires additional signatures by: $missingSigners")
    } else {
        echo("Is fully signed and ready to be sent")
    }
}

class SignatureData(val txRid: Hash, val signature: Signature) : ToGtv {
    fun encode(): String = encodeGtv(toGtv()).toHex()

    override fun toGtv() =
            gtv(mapOf("txRid" to gtv(txRid), "pubkey" to gtv(signature.subjectID), "signature" to gtv(signature.data)))

    companion object : KLogging(), FromGtv<SignatureData> {
        fun decode(transactionData: String): SignatureData = fromGtv(decodeGtv(transactionData.hexStringToByteArray()))

        override fun fromGtv(gtv: Gtv): SignatureData {
            return SignatureData(gtv["txRid"]!!.asByteArray(),
                    Signature(subjectID = gtv["pubkey"]!!.asByteArray(), data = gtv["signature"]!!.asByteArray()))
        }
    }
}
