package net.postchain.mc.cli.proposal

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.proposal_subnode_jar_extension.getSubnodeJarExtensionProposalJarFile
import net.postchain.chain0.proposal_subnode_jar_extension.getUpdateSubnodeJarExtensionProposalJarFile
import net.postchain.common.types.RowId
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.proposal.util.proposalIndexOption

class CommandDownloadJar : DCBaseCommand(
        name = "download-jar",
        help = "Download a proposed subnode JAR extension",
        requiresVersion = 102
) {
    private val id by proposalIndexOption().convert { RowId(it) }.required()

    private val save by option(help = "Where to save JAR file").file(canBeFile = true, canBeDir = false).required()

    private val update by option("--update", help = "Use this flag if proposal is to update the extension").flag()

    override fun runDC() {
        val rawJar = if (update) {
            client.getUpdateSubnodeJarExtensionProposalJarFile(id)
        } else {
            client.getSubnodeJarExtensionProposalJarFile(id)
        }
        if (rawJar == null) throw CliktError("No JAR file present for proposal with id $id")

        save.parentFile?.mkdirs()
        save.writeBytes(rawJar)
        echo("saved to ${save.name}")
    }
}
