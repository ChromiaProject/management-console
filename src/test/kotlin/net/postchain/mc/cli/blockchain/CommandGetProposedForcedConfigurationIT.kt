package net.postchain.mc.cli.blockchain

import net.postchain.chain0.proposal_blockchain.ForcedConfigurationProposalData
import net.postchain.chain0.proposal_blockchain.GET_FORCED_CONFIGURATION_STAGE2_PROPOSAL
import net.postchain.common.wrap
import net.postchain.gtv.GtvEncoder
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.test_helpers.DEFAULT_BRID_DIRECTORY_CHAIN
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandGetProposedForcedConfigurationIT {
    @Test
    fun `proposed forced configuration found`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 83)
                .withDCQuery(GET_FORCED_CONFIGURATION_STAGE2_PROPOSAL, GtvObjectMapper.toGtvDictionary(ForcedConfigurationProposalData(
                        blockchainRid = DEFAULT_BRID_DIRECTORY_CHAIN.wData,
                        currentHeight = 17,
                        currentConfiguration = GtvEncoder.encodeGtv(gtv(mapOf("foo" to gtv(17)))).wrap(),
                        proposedHeight = 23,
                        proposedConfiguration = GtvEncoder.encodeGtv(gtv(mapOf("foo" to gtv(23)))).wrap(),
                        resumeChain = false
                )))
                .testCommand(
                        CommandGetProposedForcedConfiguration(),
                        "-brid", DEFAULT_BRID_DIRECTORY_CHAIN.toHex(),
                ) { result, _ ->
                    assertCommandSuccessContains(result,
                            """Force for blockchain $DEFAULT_BRID_DIRECTORY_CHAIN at height: 23
                                
                               Path: foo
                               Value changed from 17 to 23""".trimIndent())
                }
    }

    @Test
    fun `proposed forced configuration not found`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 83)
                .withDCQuery(GET_FORCED_CONFIGURATION_STAGE2_PROPOSAL, GtvNull)
                .testCommand(
                        CommandGetProposedForcedConfiguration(),
                        "-brid", DEFAULT_BRID_DIRECTORY_CHAIN.toHex(),
                ) { result, _ ->
                    assertCommandSuccessContains(result, "No forced configuration for blockchain $DEFAULT_BRID_DIRECTORY_CHAIN")
                }
    }
}
