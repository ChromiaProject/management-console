package net.postchain.mc.cli.transaction

import com.chromia.build.tools.multisignature.MultiSignatureTxData
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.types.inputStream
import net.postchain.common.toHex
import net.postchain.crypto.PubKey
import net.postchain.gtx.Gtx
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.SignatureData
import net.postchain.mc.cli.outputFolderOption
import net.postchain.mc.cli.util.pmcKeyConfigOption

class CommandSignTransaction : PmcCommand(name = "sign", help = "Sign a saved transaction with your key(s)") {

    val config by pmcKeyConfigOption()

    val outputFolder by outputFolderOption()

    val transactionStream by argument(name = "file", help = "transaction file, or - for STDIN")
            .inputStream()

    override fun run() {
        val txData = MultiSignatureTxData.decode(transactionStream.readAllBytes().toString(Charsets.UTF_8))
        val gtx = Gtx.decode(txData.transaction)
        val missingSigners = (gtx.gtxBody.signers zip gtx.signatures).filter { it.second.isEmpty() }.map { PubKey(it.first) }.toSet()
        if (missingSigners.isEmpty()) {
            throw PrintMessage("Transaction is already fully signed")
        }
        val possessedKeys = config.clientConfig.signers.filter { missingSigners.contains(it.pubKey) }
        if (possessedKeys.isEmpty()) {
            throw CliktError("I don't have any of the keys required to sign this transaction, needs $missingSigners")
        }
        possessedKeys.forEach {
            val signature = config.clientConfig.cryptoSystem.buildSigMaker(it).signDigest(txData.txRid)
            val fileName = "signature_for_${txData.txRid.toHex()}_by_${it.pubKey}.sig"
            val file = outputFolder.resolve(fileName)
            file.writeText(SignatureData(txData.txRid, signature).encode())
            echo("Signature for ${txData.txRid.toHex()} by ${it.pubKey} is written as hex to file: ${file.absolutePath}")
        }
    }
}
