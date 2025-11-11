package net.postchain.mc.cli.jar_extension

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.proposal_subnode_jar_extension.proposeUpdateSubnodeJarExtensionOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.jarFileValidator
import net.postchain.mc.cli.util.metadataTextValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.scheduleAt

class CommandProposeUpdateSubnodeJarExtension : DCBaseCommand(
        name = "update",
        help = "Update subnode JAR extension",
        requiresVersion = 56,
) {
    private val name by nameOption("JAR extension name").required()
    private val jar by option(help = "JAR file").file(mustExist = true, mustBeReadable = true, canBeDir = false)
            .validate(jarFileValidator())
    private val extensionDescription by option("--extension-description", help = "Subnode JAR extension description")
            .validate(metadataTextValidator())
    private val gtxModules by option("-gtx", "--gtx-modules", help = "GTX modules exposed by this subnode JAR extension (comma separated list of FQCNs)")
            .validate(metadataTextValidator())
    private val syncExts by option("-sync", "--sync-exts", help = "Synchronization infrastructure extensions exposed by this subnode JAR extension (comma separated list of FQCNs)")
            .validate(metadataTextValidator())
    private val scheduledTime by scheduleAt()
    private val proposalDescription by proposalDescriptionOption { "Update subnode JAR extension $name" }

    override fun runDC() {
        transactionBuilder()
                .apply {
                    listOf(
                            jar,
                            extensionDescription,
                            gtxModules,
                            syncExts,
                    ).let {
                        if (it.all { o -> o == null }) {
                            throw CliktError("Error: at least one extension data option must be provided (--jar, --extension-description, --gtx-modules, --sync-exts)")
                        }
                    }

                    proposeUpdateSubnodeJarExtensionOperation(
                            clientProviderPubkey,
                            name,
                            jar?.readBytes(),
                            extensionDescription,
                            gtxModules,
                            syncExts,
                            proposalDescription,
                            scheduledTime,
                    )
                }
                .postOrSave()
                .printResult(
                        "Subnode JAR extension $name update proposed",
                        "Cannot propose subnode JAR extension update",
                )
    }
}
