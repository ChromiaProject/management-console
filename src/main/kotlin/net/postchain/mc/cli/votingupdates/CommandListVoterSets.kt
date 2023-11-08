package net.postchain.mc.cli.votingupdates

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getVoterSets
import net.postchain.mc.cli.interactiveOption
import net.postchain.mc.cli.promptForIndex
import net.postchain.mc.cli.util.pmcConfigOption

class CommandListVoterSets : CliktCommand(
        name = "list",
        help = "List all voter sets"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client
    private val interactive by interactiveOption()

    private val headers = listOf("Name", "Governor", "Majority level")

    override fun run() {
        val voterSets = client.getVoterSets()
        if (voterSets.isEmpty()) {
            echo("No voter sets")
        } else {
            echo("Voter sets:")
            echo(defaultTable {
                header { rowFrom(if (interactive) listOf("#") + headers else headers) }
                body {
                    voterSets.forEachIndexed { index, it ->
                        val columns = listOf(it.name, it.gorvernor, formatThreshold(it.threshold))
                        rowFrom(if (interactive) listOf(index.toString()) + columns else columns)
                    }
                }
            })
            if (interactive) {
                promptForIndex(voterSets)?.let {
                    showVoterSetInfo(client, voterSets[it].name)
                }
            }
        }
    }
}