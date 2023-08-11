package net.postchain.mc.cli.votingupdates

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.m3y.kformat.Table
import de.m3y.kformat.table
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
            table {
                header("Name", "Governor", "Majority level")
                voterSets.forEach {
                    row(it.name, it.gorvernor, formatThreshold(it.threshold))
                }
                hints {
                    borderStyle = Table.BorderStyle.SINGLE_LINE
                }
            }.render().also { echo(it) }
        }
    }
}