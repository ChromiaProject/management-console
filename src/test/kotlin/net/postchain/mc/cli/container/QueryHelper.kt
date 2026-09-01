package net.postchain.mc.cli.container

import net.postchain.chain0.common.queries.GetContainerBlockchainResult
import net.postchain.chain0.model.BlockchainState
import net.postchain.economy.economy_chain.LeaseData
import net.postchain.gtv.GtvArray
import net.postchain.gtv.GtvDictionary
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.mc.cli.test_helpers.DEFAULT_DAPP_RID

fun buildGetContainerBlockchain(): GtvArray {
    val blockchains = listOf(
            GetContainerBlockchainResult(
                    DEFAULT_DAPP_RID.wData,
                    "dapp01",
                    false,
                    BlockchainState.RUNNING,
            )
    )
    return gtv(blockchains.map(GtvObjectMapper::toGtvDictionary))
}

fun buildNmGetContainerLimits(): GtvDictionary {
    return gtv(mapOf(
            "container_units" to gtv(1),
            "max_blockchains" to gtv(10),
            "storage" to gtv(16384),
            "cpu" to gtv(50),
            "ram" to gtv(2048),
            "io_read" to gtv(25),
            "io_write" to gtv(20),
    ))
}

fun buildGetLeaseByContainerName(expireTimeMillis: Long, expired: Boolean = false): GtvDictionary {
    return GtvObjectMapper.toGtvDictionary(LeaseData(
            "container01",
            "cluster01",
            1,
            0,
            expireTimeMillis,
            expired,
            true,
            "subnode-image01",
            listOf(),
            listOf(),
            null,
            null
    ))
}
