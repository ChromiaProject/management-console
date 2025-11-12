package net.postchain.mc.cli.blockchain

import net.postchain.chain0.proposal_blockchain.GET_RESTORE_ORIGINAL_CONFIGURATION_STAGE2_PROPOSAL
import net.postchain.chain0.proposal_blockchain.PendingRestoreOriginalConfigurationData
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

class CommandGetProposedRestoreOriginalConfigurationIT {

    @Test
    fun `proposed restore original configuration found`(@TempDir dir: Path) {
        val currentCfg = GtvEncoder.encodeGtv(gtv(mapOf("foo" to gtv(1)))).wrap()
        val originalCfg = GtvEncoder.encodeGtv(gtv(mapOf("foo" to gtv(2)))).wrap()

        ManagedRestTestApi(dir, dcVersion = 104)
                .withDCQuery(
                        GET_RESTORE_ORIGINAL_CONFIGURATION_STAGE2_PROPOSAL,
                        GtvObjectMapper.toGtvDictionary(
                                PendingRestoreOriginalConfigurationData(
                                        blockchainRid = DEFAULT_BRID_DIRECTORY_CHAIN.wData,
                                        height = 55L,
                                        originalConfiguration = originalCfg,
                                        originalSigners = null,
                                        currentConfiguration = currentCfg,
                                        currentSigners = null,
                                )
                        )
                )
                .testCommand(
                        CommandGetProposedRestoreOriginalConfiguration(),
                        "-brid", DEFAULT_BRID_DIRECTORY_CHAIN.toHex(),
                ) { result, _ ->
                    assertCommandSuccessContains(
                            result,
                            "Proposed restoration of original configuration for blockchain $DEFAULT_BRID_DIRECTORY_CHAIN at height 55:"
                    )
                    // Diff should mention the change of foo from 1 to 2
                    assertCommandSuccessContains(result, "Path: foo")
                    assertCommandSuccessContains(result, "Value changed from 1 to 2")
                }
    }

    @Test
    fun `proposed restore original configuration found with signers`(@TempDir dir: Path) {
        val originalCfg = GtvEncoder.encodeGtv(gtv(mapOf("bar" to gtv(42)))).wrap()

        ManagedRestTestApi(dir, dcVersion = 104)
                .withDCQuery(
                        GET_RESTORE_ORIGINAL_CONFIGURATION_STAGE2_PROPOSAL,
                        GtvObjectMapper.toGtvDictionary(
                                PendingRestoreOriginalConfigurationData(
                                        blockchainRid = DEFAULT_BRID_DIRECTORY_CHAIN.wData,
                                        height = 77L,
                                        originalConfiguration = originalCfg,
                                        originalSigners = listOf(byteArrayOf(0x01, 0x02).wrap()),
                                        currentConfiguration = null,
                                        currentSigners = null,
                                )
                        )
                )
                .testCommand(
                        CommandGetProposedRestoreOriginalConfiguration(),
                        "-brid", DEFAULT_BRID_DIRECTORY_CHAIN.toHex(),
                ) { result, _ ->
                    assertCommandSuccessContains(
                            result,
                            "Proposed restoration of original configuration for blockchain $DEFAULT_BRID_DIRECTORY_CHAIN at height 77:"
                    )
                    // Should print a note about restored signers
                    assertCommandSuccessContains(result, "Restored signers:")
                }
    }

    @Test
    fun `proposed restore original configuration not found`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, dcVersion = 104)
                .withDCQuery(GET_RESTORE_ORIGINAL_CONFIGURATION_STAGE2_PROPOSAL, GtvNull)
                .testCommand(
                        CommandGetProposedRestoreOriginalConfiguration(),
                        "-brid", DEFAULT_BRID_DIRECTORY_CHAIN.toHex(),
                ) { result, _ ->
                    assertCommandSuccessContains(
                            result,
                            "No configuration restoration proposals for blockchain $DEFAULT_BRID_DIRECTORY_CHAIN"
                    )
                }
    }
}
