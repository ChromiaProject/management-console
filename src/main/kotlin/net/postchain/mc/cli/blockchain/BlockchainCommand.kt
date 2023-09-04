package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import net.postchain.mc.cli.blockchain.import_chain.CommandProposeFinishBlockchainImport
import net.postchain.mc.cli.blockchain.import_chain.CommandProposeImportBlockchain
import net.postchain.mc.cli.blockchain.import_chain.CommandProposeImportForeignBlocks
import net.postchain.mc.cli.blockchain.import_chain.CommandProposeImportForeignConfigurations
import net.postchain.mc.cli.replica.blockchainReplicaCommands

class BlockchainCommand : CliktCommand("Interactions with blockchains") {
    override fun run() = Unit
}

fun blockchainCommands() = BlockchainCommand().subcommands(
        CommandProposeBlockchain(),
        // import
        CommandProposeImportBlockchain(),
        CommandProposeFinishBlockchainImport(),
        // foreign import
        CommandProposeImportForeignConfigurations(),
        CommandProposeImportForeignBlocks(),

        CommandProposeConfiguration(),
        CommandProposePauseBlockchain(),
        CommandProposeResumeBlockchain(),
        CommandProposeDeleteBlockchain(),
        CommandListBlockchainReplicas(),
        CommandListBlockchainSigners(),
        CommandListBlockchains(),
        CommandGetAllBlockchainConfigurations(),
        CommandGetBlockchainConfiguration(),
        CommandGetBlockchainConfigurationDeprecated(),
        CommandGetProposedBlockchainRid(),
        blockchainReplicaCommands()
)