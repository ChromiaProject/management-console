package net.postchain.mc.cli.votingupdates

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import net.postchain.chain0.common.queries.getVoterSets
import net.postchain.mc.cli.util.pmcConfigOption

class CommandListVoterSets : CliktCommand(
        name = "list",
        help = "List all voter sets"
) {
    private val config by pmcConfigOption()
    private val client get() = config.client

    override fun run() {
        val voterSets = client.getVoterSets()
        if (voterSets.isEmpty()) {
            echo("No voter sets")
        } else {
            echo("Voter sets:")
            echo(defaultTable {
                header { row("Name", "Governor", "Majority level") }
                body {
                    voterSets.forEach {
                        row(it.name, it.gorvernor, formatThreshold(it.threshold))
                    }
                }
            })
        }
    }
}