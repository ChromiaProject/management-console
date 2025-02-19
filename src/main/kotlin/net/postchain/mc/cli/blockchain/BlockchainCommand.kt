package net.postchain.mc.cli.blockchain

import com.github.ajalt.clikt.core.subcommands
import net.postchain.mc.cli.PmcCommand
import net.postchain.mc.cli.blockchain.import_chain.CommandProposeFinishBlockchainImport
import net.postchain.mc.cli.blockchain.import_chain.CommandProposeImportBlockchain
import net.postchain.mc.cli.blockchain.import_chain.CommandProposeImportForeignBlocks
import net.postchain.mc.cli.blockchain.import_chain.CommandProposeImportForeignConfigurations
import net.postchain.mc.cli.blockchain.move.CommandProposeBlockchainMove
import net.postchain.mc.cli.blockchain.move.CommandProposeCancelBlockchainMove
import net.postchain.mc.cli.blockchain.move.CommandProposeFinishBlockchainMove
import net.postchain.mc.cli.replica.blockchainReplicaCommands

class BlockchainCommand : PmcCommand(help = "Interactions with blockchains") {
    override fun run() = Unit
}

@Suppress("DEPRECATION")
fun blockchainCommands() = BlockchainCommand().subcommands(
        CommandProposeBlockchain(),
        // import
        CommandProposeImportBlockchain(),
        CommandProposeFinishBlockchainImport(),
        // foreign import
        CommandProposeImportForeignConfigurations(),
        CommandProposeImportForeignBlocks(),

        CommandProposeConfiguration(),
        CommandProposeForcedConfiguration(),
        CommandProposePauseBlockchain(),
        CommandProposeResumeBlockchain(),
        CommandProposeDeleteBlockchain(),
        CommandProposeRenameBlockchain(),
        CommandListBlockchainReplicas(),
        CommandListBlockchainSigners(),
        CommandListBlockchains(),
        CommandListDelayedConfigurations(),
        CommandGetBlockchainInfo(),
        CommandGetAllBlockchainConfigurations(),
        CommandGetBlockchainConfiguration(),
        CommandGetBlockchainConfigurationDeprecated(),
        CommandGetForcedConfigurations(),
        CommandBlockchainConfigurationDiff(),
        CommandGetProposedBlockchainRid(),
        blockchainReplicaCommands(),

        CommandProposeBlockchainMove(),
        CommandProposeCancelBlockchainMove(),
        CommandProposeFinishBlockchainMove(),

        CommandProposeArchiveBlockchain(),
        CommandProposeUnarchiveBlockchain()
)