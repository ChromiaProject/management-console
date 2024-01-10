package net.postchain.mc.cli.votingupdates

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.required
import net.postchain.chain0.common.queries.getVoterSetInfo
import net.postchain.client.core.PostchainClient
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandVoterSetInfo : CliktCommand(
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

fun CliktCommand.showVoterSetInfo(client: PostchainClient, name: String) {
    val voterSet = client.getVoterSetInfo(name)
    echo(pmcTable {
        body {
            row("Voter set", voterSet.name)
            row("Governed by", voterSet.governor)
            row("Threshold", formatThreshold(voterSet.threshold))
            voterSet.members.forEachIndexed { index, bytes -> row("Member $index", bytes.toHex()) }
        }
    })
}
