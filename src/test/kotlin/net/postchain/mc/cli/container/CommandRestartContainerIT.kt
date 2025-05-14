package net.postchain.mc.cli.container

import assertk.assertThat
import assertk.assertions.contains
import net.postchain.api.rest.BlockHeight
import net.postchain.api.rest.controller.NotFoundError
import net.postchain.chain0.common.queries.GetContainerBlockchainResult
import net.postchain.chain0.model.BlockchainState
import net.postchain.chain0.proposal_container.ContainerAction
import net.postchain.chain0.proposal_container.proposeContainerActionOperation
import net.postchain.chain0.proposal_container.resumeContainerOperation
import net.postchain.common.hexStringToByteArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.test_helpers.DEFAULT_DAPP_RID
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi.Companion.DEFAULT_PROVIDER_PUBKEY
import net.postchain.mc.cli.test_helpers.RestTestModel
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.mock
import java.nio.file.Path
import kotlin.time.Duration.Companion.seconds

class CommandRestartContainerIT {

    @Test
    fun `restart container - success`(@TempDir dir: Path) {
        val startTime = System.nanoTime()
        val doneTime = startTime + 5.seconds.inWholeNanoseconds
        ManagedRestTestApi(dir, dcVersion = 94)
                .withDCQuery("get_container_blockchain", gtv(listOf(GtvObjectMapper.toGtvDictionary(
                        GetContainerBlockchainResult(
                                rid = DEFAULT_DAPP_RID.wData,
                                name = "chain1",
                                system = false,
                                state = BlockchainState.RUNNING)
                ))))
                .withExtraModel(DEFAULT_DAPP_RID, "container1", object : RestTestModel(chainIID = 100, model = mock()) {
                    override fun getCurrentBlockHeight(): BlockHeight {
                        if (System.nanoTime() > doneTime) throw NotFoundError("Chain is paused")
                        return BlockHeight(17)
                    }
                })
                .testCommand(
                        CommandRestartContainer(),
                        "--name", "container1",
                ) { result, api ->
                    assertThat(result.output).contains("Container resumed successfully")
                    api.getDcModel().assertCalledOps {
                        it.proposeContainerActionOperation(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "container1",
                                ContainerAction.pause,
                                "Pause container container1"
                        )
                        it.resumeContainerOperation(
                                DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray(),
                                "container1",
                        )
                    }
                }
    }
}
