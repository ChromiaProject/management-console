package net.postchain.mc.cli.base

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintMessage
import net.postchain.client.config.PostchainClientConfig
import net.postchain.client.core.PostchainClient
import net.postchain.client.core.TransactionResult
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.client.impl.PostchainClientProviderImpl
import net.postchain.common.tx.TransactionStatus
import net.postchain.mc.cli.util.NopPostchainClient

fun TransactionResult.printResult(onSuccess: String, onFail: String, printOnSuccess: Boolean = false) {
    return when (status) {
        TransactionStatus.CONFIRMED -> if (printOnSuccess) println(onSuccess) else throw PrintMessage(onSuccess)
        TransactionStatus.REJECTED -> throw CliktError("$onFail: $rejectReason")
        TransactionStatus.WAITING -> throw PrintMessage("Transaction $txRid was sent to transaction queue")
        else -> throw CliktError("Cannot find status for this transaction")
    }
}
