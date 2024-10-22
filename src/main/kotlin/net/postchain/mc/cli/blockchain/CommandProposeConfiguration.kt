package net.postchain.mc.cli.blockchain

import com.chromia.build.tools.config.BlockchainConfigurationCompressor
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import net.postchain.chain0.proposal_blockchain.proposeConfigurationAtOperation
import net.postchain.chain0.proposal_blockchain.proposeConfigurationOperation
import net.postchain.gtv.GtvEncoder
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.mc.cli.AlreadyExistMode
import net.postchain.mc.cli.DCBaseCommand
import net.postchain.mc.cli.base.printResult
import net.postchain.mc.cli.blockchainRidOption
import net.postchain.mc.cli.forceOption
import net.postchain.mc.cli.heightOption
import net.postchain.mc.cli.util.BlockchainConfig
import net.postchain.mc.cli.util.proposalDescriptionOption


class CommandProposeConfiguration : DCBaseCommand(
        name = "update",
        help = """
        Propose a new configuration to blockchain
        
        In the case of chain0 additional height argument can be specified. 
        Height must be > current height and > all previously approved configuration heights.
        Use force flag -f to override previously added configs or to squeeze in a configuration 
        at a height < previously approved config heights. Change will be applied after voting.
        """.trimIndent()
) {
    private val blockchainConfigFile by option("-bc", "--blockchain-config", help = "Blockchain config to propose")
            .file(mustExist = true, mustBeReadable = true, canBeDir = false)
            .required()

    private val blockchainRID by blockchainRidOption().required()

    private val height by heightOption()

    private val force by forceOption()

    private val description by proposalDescriptionOption {
        if (height == null) "Update of blockchain configuration for $blockchainRID"
        else "Update of blockchain configuration for $blockchainRID at height $height with force: $force"
    }

    override fun runDC() {
        val bcConfig = BlockchainConfig.readFromFile(blockchainConfigFile)
        val compressedConfigurationData = GtvEncoder.encodeGtv(BlockchainConfigurationCompressor.compress(client, bcConfig.gtv, dcVersion))

        client.transactionBuilder()
                .apply {
                    if (height == null) {
                        when (dcVersion) {
                            1L -> {
                                addOperation("propose_configuration",
                                        gtv(clientProviderPubkey),
                                        gtv(blockchainRID),
                                        gtv(bcConfig.data)
                                )
                            }

                            else -> {
                                proposeConfigurationOperation(clientProviderPubkey, blockchainRID, compressedConfigurationData, description)
                            }
                        }
                    } else {
                        when (dcVersion) {
                            1L -> {
                                addOperation("propose_configuration_at",
                                        gtv(clientProviderPubkey),
                                        gtv(blockchainRID),
                                        gtv(bcConfig.data),
                                        gtv(height!!),
                                        gtv(force == AlreadyExistMode.FORCE))
                            }

                            else -> {
                                proposeConfigurationAtOperation(clientProviderPubkey, blockchainRID, compressedConfigurationData, height!!, force == AlreadyExistMode.FORCE, description)
                            }
                        }
                    }
                }
                .postAwaitConfirmation()
                .printResult(
                        "Configuration was proposed: ${bcConfig.hash}",
                        "Failed to propose configuration"
                )
    }
}
