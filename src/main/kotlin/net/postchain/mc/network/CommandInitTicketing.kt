package net.postchain.mc.network

import com.chromia.cli.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.ticketing.initTicketingOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.base.pubkey
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.pmcConfigOption

class CommandInitTicketing : CliktCommand(
        name = "initialize-ticketing",
        help = "Create and initialize ticket chain"
) {

    private val config by pmcConfigOption()
    private val client get() = config.client

    private val ticketChainConfig by option(
            "-tcc",
            "--ticket-chain-config",
            help = "Configuration file for ticket chain (GtvML (*.xml) or Gtv (*.gtv))"
    ).file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    override fun run() {
        val version = Version(client).version
        if (version < 16) {
            echo("Ticketing requires directory chain version 16, found version $version")
            return
        }

        ticketChainConfig?.let {
            val ticketChainConfigData = BlockchainConfig.readFromFile(it)
            val compressedTicketChainConfig = BlockchainConfigurationCompressor.compress(client, ticketChainConfigData.gtv, version)
            client.transactionBuilder()
                    .initTicketingOperation(client.pubkey, GtvEncoder.encodeGtv(compressedTicketChainConfig))
                    .postAwaitConfirmation()
                    .printResult(
                            "Ticket chain was created",
                            "Failed to create ticket chain",
                            printOnSuccess = true
                    )
        }

        initTicketChain(client, config.config)
    }
}
