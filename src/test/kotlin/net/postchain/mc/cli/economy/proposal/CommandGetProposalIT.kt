package net.postchain.mc.cli.economy.proposal

import net.postchain.common.types.WrappedByteArray
import net.postchain.economy.economy_chain.PendingClusterData
import net.postchain.economy.economy_chain.PendingClusterStatus
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.base.ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION
import net.postchain.mc.cli.economy.addEcGetCommonProposal
import net.postchain.mc.cli.economy.addEcGetCommonProposalVotingResult
import net.postchain.mc.cli.provider.addDcGetProviderData
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.assertCommandSuccessContains
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommandGetProposalIT {

    @Test
    fun `cluster info - container units`(@TempDir dir: Path) {
        ManagedRestTestApi(dir, ecVersion = ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION)
                .addEcGetCommonProposal()
                .addEcGetCommonProposalVotingResult()
                .withECQuery("get_cluster_create_proposal", GtvObjectMapper.toGtvDictionary(PendingClusterData(
                        "name", "tag", WrappedByteArray(34), "governor-vs", "vs", 20, 0, 40, 1500, 18, 10, 11000, 5, PendingClusterStatus.PENDING_CREATION
                )))
                .addDcGetProviderData()
                .testCommand(
                        CommandGetProposal(),
                        "--id", "123",
                ) { result, _ ->
                    assertCommandSuccessContains(result, """
                        {
                          "Name": "name",
                          "Tag": "tag",
                          "Proposer": "00000000000000000000000000000000000000000000000000000000000000000000",
                          "Governor_voter_set": "governor-vs",
                          "Voter_set": "vs",
                          "Cluster_units": "20",
                          "Extra_storage": "0",
                          "Container_unit_-_CPU": "40",
                          "Container_unit_-_RAM": "1500",
                          "Container_unit_-_storage": "11000",
                          "Container_unit_-_I/O_read": "18",
                          "Container_unit_-_I/O_write": "10",
                          "System_container_units": "5",
                          "Status": "PENDING_CREATION"
                        }
                    """.trimIndent())
                }
    }
}
