package net.postchain.mc.cli.blockchain

import net.postchain.chain0.proposal_blockchain.GET_REMOVE_FORCED_CONFIGURATION_STAGE2_PROPOSAL
import net.postchain.chain0.proposal_blockchain.PendingRemoveForcedConfigurationData
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.test_helpers.DEFAULT_BRID_DIRECTORY_CHAIN
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandGetProposedRemoveForcedConfigurationIT {

    @Test
    fun `proposed remove forced configuration found`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 104)
                .withDCQuery(
                        GET_REMOVE_FORCED_CONFIGURATION_STAGE2_PROPOSAL,
                        GtvObjectMapper.toGtvDictionary(
                                PendingRemoveForcedConfigurationData(
                                        blockchainRid = DEFAULT_BRID_DIRECTORY_CHAIN.wData,
                                        height = 123L,
                                )
                        )
                )
                .testCommand(
                        CommandGetProposedRemoveForcedConfiguration(),
                        "-brid", DEFAULT_BRID_DIRECTORY_CHAIN.toHex(),
                ) { result, _ ->
                    assertCommandSuccessContains(
                            result,
                            "Remove forced configuration(s) for blockchain $DEFAULT_BRID_DIRECTORY_CHAIN at height: 123"
                    )
                }
    }

    @Test
    fun `proposed remove forced configuration not found`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 104)
                .withDCQuery(GET_REMOVE_FORCED_CONFIGURATION_STAGE2_PROPOSAL, GtvNull)
                .testCommand(
                        CommandGetProposedRemoveForcedConfiguration(),
                        "-brid", DEFAULT_BRID_DIRECTORY_CHAIN.toHex(),
                ) { result, _ ->
                    assertCommandSuccessContains(
                            result,
                            "No forced configuration removal proposals for blockchain $DEFAULT_BRID_DIRECTORY_CHAIN"
                    )
                }
    }
}
