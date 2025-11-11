package net.postchain.mc.cli.jar_extension

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.model.SubnodeJarExtensionType
import net.postchain.chain0.proposal_subnode_jar_extension.proposeSubnodeJarExtensionOperation
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.util.entityNameValidator
import net.postchain.mc.cli.util.jarFileValidator
import net.postchain.mc.cli.util.metadataTextValidator
import net.postchain.mc.cli.util.nameOption
import net.postchain.mc.cli.util.proposalDescriptionOption
import net.postchain.mc.cli.util.scheduleAt

class CommandProposeSubnodeJarExtension : DCBaseCommand(
        name = "add",
        help = "Register new subnode JAR extension",
        requiresVersion = 102,
) {
    private val name by nameOption("JAR extension name").required().validate(entityNameValidator())
    private val jar by option(help = "JAR file").file(mustExist = true, mustBeReadable = true, canBeDir = false)
            .required().validate(jarFileValidator())
    private val type by option(help = "JAR extension type").enum<SubnodeJarExtensionType>().default(SubnodeJarExtensionType.COMMON)
    private val extensionDescription by option("-desc", "--extension-description", help = "Subnode JAR extension description").required()
            .validate(metadataTextValidator())
    private val gtxModules by option("-gtx", "--gtx-modules", help = "GTX modules exposed by this subnode JAR extension (comma separated list of FQCNs)")
            .default("").validate(metadataTextValidator())
    private val syncExts by option("-sync", "--sync-exts", help = "Synchronization infrastructure extensions exposed by this subnode JAR extension (comma separated list of FQCNs)")
            .default("").validate(metadataTextValidator())
    private val scheduledTime by scheduleAt()
    private val description by proposalDescriptionOption { "Register new subnode JAR extension $name" }

    override fun runDC() {
        transactionBuilder()
                .apply {
                    proposeSubnodeJarExtensionOperation(clientProviderPubkey, name, jar.readBytes(), type, extensionDescription,
                            gtxModules, syncExts, description, scheduledTime)
                }
                .postOrSave()
                .printResult(
                        "Subnode JAR extension $name proposed",
                        "Cannot propose subnode JAR extension"
                )
    }
}
