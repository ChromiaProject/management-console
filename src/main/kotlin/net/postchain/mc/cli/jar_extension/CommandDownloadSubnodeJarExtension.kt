package net.postchain.mc.cli.jar_extension

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.nm_api.nmGetJarExtension
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.util.nameOption

class CommandDownloadSubnodeJarExtension : DCBaseCommand(
        name = "download-jar",
        help = "Download the raw JAR file for a subnode JAR extension",
        requiresVersion = 102
) {
    private val name by nameOption("Subnode JAR extension name").required()

    private val save by option(help = "Where to save JAR file").file(canBeFile = true, canBeDir = false)

    override fun runDC() {
        val rawJar = client.nmGetJarExtension(name)

        if (save != null) {
            save!!.parentFile?.mkdirs()
            save!!.writeBytes(rawJar)
            echo("saved to ${save!!.name}")
        } else {
            echo(rawJar)
        }
    }
}
