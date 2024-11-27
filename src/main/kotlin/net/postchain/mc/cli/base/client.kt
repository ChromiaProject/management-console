package net.postchain.mc.cli.base

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintMessage
import net.postchain.client.core.TransactionResult
import net.postchain.common.tx.TransactionStatus

fun TransactionResult.printResult(onSuccess: String, onFail: String, printOnSuccess: Boolean = false) {
    return when (status) {
        TransactionStatus.CONFIRMED -> {
            val message = "$onSuccess - TxRID: ${txRid.rid}"
            if (printOnSuccess) println(message) else throw PrintMessage(message, statusCode = 0)
        }
        TransactionStatus.REJECTED -> throw CliktError("$onFail: $rejectReason")
        TransactionStatus.WAITING ->
            throw PrintMessage("Transaction was sent to transaction queue - TxRID: ${txRid.rid}", statusCode = 2)
        else -> throw CliktError("Cannot find status for this transaction")
    }
}
