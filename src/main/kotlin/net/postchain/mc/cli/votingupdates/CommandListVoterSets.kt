package net.postchain.mc.cli.votingupdates

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.mordant.input.interactiveSelectList
import net.postchain.chain0.common.queries.getVoterSets
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption
import net.postchain.mc.cli.util.pmcTable

class CommandListVoterSets : PmcCommand(
        name = "list",
        help = "List all voter sets",
        printHelpOnEmptyArgs = false
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()

    private val headers = listOf("Name", "Governor", "Majority level")

    override fun run() {
        val voterSets = client.getVoterSets()
        if (interactive && voterSets.isNotEmpty() && voterSets.size < terminal.size.height) {
            terminal.interactiveSelectList(voterSets.map {
                "${it.name} - ${formatThreshold(it.threshold)}"
            }, "Select voter set")?.let {
                showVoterSetInfo(client, it.split(' ').first())
            }
        } else {
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
}
