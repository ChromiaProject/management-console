package net.postchain.mc.cli.base

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintMessage
import net.postchain.client.core.TransactionResult
import net.postchain.common.tx.TransactionStatus

fun TransactionResult.printResult(onSuccess: String, onFail: String, printOnSuccess: Boolean = false) {
    return when (status) {
        TransactionStatus.CONFIRMED -> if (printOnSuccess) println(onSuccess) else throw PrintMessage(onSuccess, statusCode = 0)
        TransactionStatus.REJECTED -> throw CliktError("$onFail: $rejectReason")
        TransactionStatus.WAITING -> throw PrintMessage("Transaction $txRid was sent to transaction queue", statusCode = 0)
        else -> throw CliktError("Cannot find status for this transaction")
    }
}
