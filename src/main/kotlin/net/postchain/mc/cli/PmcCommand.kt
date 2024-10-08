package net.postchain.mc.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context

abstract class PmcCommand(
        name: String? = null,
        private val help: String? = null,
        override val printHelpOnEmptyArgs: Boolean = false
): CliktCommand(name) {
    override fun help(context: Context): String = help ?: ""
}
