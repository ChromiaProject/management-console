package net.postchain.mc.network

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.ticketing.initTicketingOperation
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.pmcConfigOption

class CommandInitTicketing : CliktCommand(
        name = "initialize-ticketing",
        help = "Create ticket chain"
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val ticketChainConfig by option(
            "-tcc",
            "--ticket-chain-config",
            help = "Configuration file for ticket chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true).required()

    override fun run() {
        val version = Version(client).version
        if (version < 16) {
            echo("Ticketing requires directory chain version 16, found version $version")
            return
        }

        val ticketChainConfigData = BlockchainConfig.readFromFile(ticketChainConfig).data

        client.transactionBuilder()
                .initTicketingOperation(client.pubkey, ticketChainConfigData)
                .postAwaitConfirmation()
                .printResult(
                        "Ticket chain was created",
                        "Failed to create ticket chain"
                )
    }
}
