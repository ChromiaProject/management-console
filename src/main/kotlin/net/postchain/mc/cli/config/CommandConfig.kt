package net.postchain.mc.cli.config

import com.chromia.cli.tools.config.ChromiaConfigLoader
import com.chromia.cli.tools.config.ChromiaConfigWriter
import com.chromia.cli.tools.env.cliEnv
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.groups.default
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.mc.cli.util.CHROMIA_CONFIG
import java.awt.Desktop


fun CliktCommand.configFileOption() = mutuallyExclusiveOptions(
        option("--global", help = "use global configuration file").flag().convert { ChromiaConfigWriter.global },
        option("--local", help = "use project configuration file").flag().convert { ChromiaConfigWriter.local },
        option("--file", envvar = CHROMIA_CONFIG, help = "use given configuration file (env: $CHROMIA_CONFIG)")
                .file(mustExist = true, canBeDir = false).convert { ChromiaConfigWriter.custom(it) },
        name = "Config file location",
).default(ChromiaConfigWriter.local)

class CommandConfig : CliktCommand(
        name = "config",
        help = "Configure the management console"
) {

    private val configWriter by configFileOption()
    private val configLoader = ChromiaConfigLoader(cliEnv())
    private val configFile get() = configWriter.configFile

    private val get by option(help = "get value: name [value pattern]", metavar = "KEY")

    private val edit by option("-e", "--edit", help = "edit file using default editor").flag()

    private val list by option(help = "list all").flag()

    private val set by option("--set", help = "set values [key=value]", metavar = "KEY=VALUE").associate()

    override fun run() {
        if (list) {
            configFile.readLines()
                    .joinToString("\n") { if (it.startsWith("privkey")) "privkey=********************************" else it }
                    .also { return echo(it) }
        }
        if (edit) {
            if (!Desktop.isDesktopSupported()) throw IllegalArgumentException("Cannot edit file interactively, set parameters one by one")
            return Desktop.getDesktop().edit(configFile)
        }
        if (get != null) {
            if (get == "privkey") throw CliktError("Cannot print private key to stdout")
            return echo(configLoader.loadClientConfigFile(configFile).getString(get))
        }
        if (set.isNotEmpty()) {
            configWriter.setProperty(set)
        }
    }
}
