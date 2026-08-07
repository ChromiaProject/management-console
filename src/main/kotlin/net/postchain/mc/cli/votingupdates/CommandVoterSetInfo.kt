package net.postchain.mc.cli.votingupdates

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.mordant.rendering.TextAlign
import net.postchain.chain0.common.queries.getAllProviders
import net.postchain.chain0.common.queries.getVoterSetInfo
import net.postchain.client.core.PostchainReadClient
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandVoterSetInfo : PmcCommand(
        name = "info",
        help = "Show information of voter set"
) {
    private val config by pmcConfigOption()
    val client get() = config.client

    private val name by nameOption("Name of voter set").required()

    override fun run() {
        showVoterSetInfo(client, name)
    }
}

fun CliktCommand.showVoterSetInfo(client: PostchainReadClient, name: String) {
    val voterSet = client.getVoterSetInfo(name)
    val providerNames = client.getAllProviders().associate { it.pubkey.toHex() to it.name }

    if (!terminal.terminalInfo.outputInteractive) {
        echo("{")
        echo(""""basic": """, trailingNewline = false)
    }

    echo(pmcTable {
        captionTop("Basic info:", TextAlign.LEFT)
        body {
            row("Voter set", voterSet.name)
            row("Governed by", voterSet.governor)
            row("Threshold", formatThreshold(voterSet.threshold))
        }
    })

    if (!terminal.terminalInfo.outputInteractive) {
        echo(""","providers": """, trailingNewline = false)
    }
    echo(pmcTable(
            "providers",
            listOf("Name", "Pubkey"),
            voterSet.members.map { listOf(providerNames[it.toHex()] ?: "", it.toHex()) },
            null,
            // Numbers the providers in the interactive table, the JSON output is a plain array
            interactive = true
    ))

    if (!terminal.terminalInfo.outputInteractive) {
        echo("}")
    }
}
