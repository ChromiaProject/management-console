package net.postchain.mc.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.terminal
import net.postchain.client.core.PollingTransactionStatus
import net.postchain.client.core.PostingTransaction
import net.postchain.client.core.TransactionConfirmed
import net.postchain.client.core.TransactionPollingRejected
import net.postchain.client.core.TransactionPollingTimeout
import net.postchain.client.core.TransactionPostedRejected
import net.postchain.client.core.TransactionPostedSuccessfully
import net.postchain.client.core.TxEventListener

abstract class PmcCommand(
        name: String? = null,
        private val help: String? = null,
        override val printHelpOnEmptyArgs: Boolean = false
): CliktCommand(name) {
    override fun help(context: Context): String = help ?: ""

    fun txListener() = TxEventListener {
        if (terminal.terminalInfo.outputInteractive) {
            when (it) {
                is PostingTransaction -> echo("Posting transaction...", trailingNewline = false)
                is TransactionPostedSuccessfully -> {
                    echo("success")
                    echo("Polling for transactions status...", trailingNewline = false)
                }

                is TransactionPostedRejected -> echo("rejected")
                is PollingTransactionStatus -> echo(" #", trailingNewline = false)
                is TransactionConfirmed -> echo(" confirmed")
                is TransactionPollingRejected -> echo(" rejected")
                is TransactionPollingTimeout -> echo(" timeout")
            }
        }
    }
}
