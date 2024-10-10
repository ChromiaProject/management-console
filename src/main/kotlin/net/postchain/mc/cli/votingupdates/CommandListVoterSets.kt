package net.postchain.mc.cli.votingupdates

import net.postchain.mc.cli.PmcCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getVoterSets
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandListVoterSets : PmcCommand(
        name = "list",
        help = "List all voter sets"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()

    private val headers = listOf("Name", "Governor", "Majority level")

    override fun run() {
        val voterSets = client.getVoterSets()
        echo(pmcTable(
                "voter sets",
                headers,
                voterSets.map {
                    listOf(it.name, it.gorvernor, formatThreshold(it.threshold))
                },
                null,
                interactive
        ))
        if (interactive && voterSets.isNotEmpty()) {
            promptForIndex(voterSets)?.let {
                showVoterSetInfo(client, voterSets[it].name)
            }
        }
    }
}
