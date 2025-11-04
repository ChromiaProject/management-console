package net.postchain.mc.cli

import com.chromia.build.tools.config.SUPPRESS_KEY_STORAGE_DEPRECATION_WARNING_SYSTEM_PROPERTY
import com.chromia.cli.tools.launcher.CliLauncher
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import net.postchain.mc.cli.anchoring.clusterAnchoringCommands
import net.postchain.mc.cli.blockchain.blockchainCommands
import net.postchain.mc.cli.cluster.clusterCommands
import net.postchain.mc.cli.config.CommandConfig
import net.postchain.mc.cli.container.containerCommands
import net.postchain.mc.cli.economy.economyCommands
import net.postchain.mc.cli.image.subnodeImageCommands
import net.postchain.mc.cli.keys.CommandKeygen
import net.postchain.mc.cli.lease.leaseCommands
import net.postchain.mc.cli.node.nodeCommands
import net.postchain.mc.cli.proposal.proposalCommands
import net.postchain.mc.cli.provider.providerCommands
import net.postchain.mc.cli.votingupdates.voterSetCommands
import net.postchain.mc.network.networkCommands
import net.postchain.mc.cli.base.VersionChecker.checkAndWarnIfOutdated
import net.postchain.mc.cli.jar_extension.subnodeJarExtensionCommands

open class ManagementConsole : CliLauncher(name = "pmc") {

    init {
        System.setProperty(SUPPRESS_KEY_STORAGE_DEPRECATION_WARNING_SYSTEM_PROPERTY, "true")

        val currentVersion = this::class.java.`package`.implementationVersion ?: "(unknown)"
        val version = """
            ${currentVersion}
            Java version ${System.getProperty("java.version")}
        """.trimIndent()

        // do check latest version when pmc execution
        checkAndWarnIfOutdated(currentVersion)

        versionOption(version)
        subcommands(
                HelpCommand(),
                VersionCommand("pmc", version),
                CommandKeygen(),
                CommandConfig(),
                networkCommands(),
                nodeCommands(),
                providerCommands().also { extraProviderCommands(it) },
                proposalCommands(),
                voterSetCommands(),
                clusterCommands(),
                containerCommands(),
                blockchainCommands(),
                clusterAnchoringCommands(),
                economyCommands(),
                subnodeImageCommands(),
                leaseCommands(),
                subnodeJarExtensionCommands()
        )
    }

    protected open fun extraProviderCommands(command: CliktCommand) {}

    override fun aliases(): Map<String, List<String>> {
        return mapOf(
                "init" to listOf("network", "initialize"),
                "initialize" to listOf("network", "initialize"),
                "blockchains" to listOf("blockchain", "list"),
                "bcs" to listOf("blockchain", "list"),
                "votersets" to listOf("voterset", "list"),
                "containers" to listOf("container", "list"),
                "clusters" to listOf("cluster", "list"),
                "providers" to listOf("provider", "list"),
                "proposals" to listOf("proposal", "list"),
                "nodes" to listOf("node", "list"),
                "subnode-images" to listOf("subnode-image", "list"),
                "leases" to listOf("lease", "list"),
        )
    }
}
