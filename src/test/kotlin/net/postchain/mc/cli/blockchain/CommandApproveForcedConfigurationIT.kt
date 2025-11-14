package net.postchain.mc.cli.blockchain

import assertk.assertThat
import assertk.assertions.isTrue
import net.postchain.chain0.proposal_blockchain.APPROVE_PROPOSED_FORCED_CONFIGURATION
import net.postchain.chain0.proposal_blockchain.ForcedConfigurationProposalData
import net.postchain.common.wrap
import net.postchain.gtv.GtvEncoder
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.test_helpers.DEFAULT_BRID_ECONOMY_CHAIN
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandApproveForcedConfigurationIT {

    @Test
    fun `approve forced configuration`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 85)
                .withDCQuery("get_forced_configuration_stage2_proposal", GtvObjectMapper.toGtvDictionary(ForcedConfigurationProposalData(
                        blockchainRid = DEFAULT_BRID_ECONOMY_CHAIN.wData,
                        currentHeight = 567,
                        currentConfiguration = GtvEncoder.encodeGtv(gtv(mapOf("foo" to gtv(17)))).wrap(),
                        proposedHeight = 567,
                        proposedConfiguration = GtvEncoder.encodeGtv(gtv(mapOf("foo" to gtv(23)))).wrap(),
                        resumeChain = false
                )))
                .withECModel { it.height = 567 }
                .testCommand(
                        CommandApproveForcedConfiguration(),
                        "-brid", DEFAULT_BRID_ECONOMY_CHAIN.toHex(),
                ) { result, api ->
                    assertCommandSuccessContains(result, "Forced configurations was approved")
                    assertThat(api.getDcModel().opWasCalled(APPROVE_PROPOSED_FORCED_CONFIGURATION) {
                        it[1].asByteArray().contentEquals(DEFAULT_BRID_ECONOMY_CHAIN.data)
                    }).isTrue()
                }
    }
}
