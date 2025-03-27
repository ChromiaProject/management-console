package net.postchain.mc.cli.blockchain

import net.postchain.anchoring.anchoring_chain_common.AnchorBlock
import net.postchain.chain0.cm_api.CmClusterInfo
import net.postchain.chain0.cm_api.CmPeerInfo
import net.postchain.chain0.common.queries.BlockchainInfo
import net.postchain.chain0.common.queries.GetMovingBlockchainInfoResult
import net.postchain.chain0.common.queries.GetUnarchivingBlockchainInfoResult
import net.postchain.chain0.common.queries.NodeData
import net.postchain.chain0.model.BlockchainState
import net.postchain.chain0.model.ImportingForeignBlockchain
import net.postchain.common.BlockchainRid
import net.postchain.common.hexStringToByteArray
import net.postchain.common.types.RowId
import net.postchain.common.wrap
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvDictionary
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtx.GtxQuery
import net.postchain.mc.cli.test_helpers.DEFAULT_NODE01_API
import net.postchain.mc.cli.test_helpers.DEFAULT_NODE01_HOST
import net.postchain.mc.cli.test_helpers.DEFAULT_NODE01_PORT
import net.postchain.mc.cli.test_helpers.DEFAULT_NODE01_PUBKEY
import net.postchain.mc.cli.test_helpers.DEFAULT_PROVIDER01_PUBKEY
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi.Companion.DEFAULT_PROVIDER_PUBKEY

fun buildGetBlockchainInfoResponse(
        isForeignImporting: Boolean = false,
        isMoving: Boolean = false,
        isUnarchiving: Boolean = false,
        configDelay: Long? = null,
        state: BlockchainState = BlockchainState.RUNNING,
): GtvDictionary {
    return GtvObjectMapper.toGtvDictionary(BlockchainInfo(
            BlockchainRid.ZERO_RID.wData,
            "bc01",
            state,
            "container01",
            "cluster01",
            false,
            isForeignImporting,
            isMoving,
            isUnarchiving,
            configDelay
    ))
}

fun buildNmFindNextConfigurationHeightResponse(configHeights: List<Int>): (query: GtxQuery) -> Gtv {
    return { query ->
        val height = query.args["height"]!!.asInteger()
        val nextConfig = configHeights.firstOrNull { height < it }
        nextConfig?.let { gtv(it.toLong()) } ?: GtvNull
    }
}

fun buildGetBlockchainReplicasResponse(): Gtv {
    return gtv(listOf(gtv(listOf(
            gtv(DEFAULT_NODE01_PUBKEY.wData),
            gtv(DEFAULT_NODE01_HOST),
            gtv(DEFAULT_NODE01_PORT),
            gtv(true),
            gtv(0)
    ))))
}

fun buildGetMovingBlockchainInfoResponse(): Gtv {
    return GtvObjectMapper.toGtvDictionary(GetMovingBlockchainInfoResult(
            "container01",
            "container02",
            15000L,
    ))
}

fun buildGetImportingForeignBlockchainInfoResponse(): Gtv {
    return GtvObjectMapper.toGtvDictionary(ImportingForeignBlockchain(
            BlockchainRid.ZERO_RID.wData,
            DEFAULT_NODE01_PUBKEY.wData,
            DEFAULT_NODE01_HOST,
            DEFAULT_NODE01_PORT,
            DEFAULT_NODE01_API,
            BlockchainRid.ZERO_RID.wData,
            16000L
    ))
}

fun buildGetUnarchivingBlockchainInfo(): Gtv {
    return GtvObjectMapper.toGtvDictionary(GetUnarchivingBlockchainInfoResult(
            "container01",
            "container02",
            15000L,
    ))
}

fun buildGetBlockchainInfoListResponse(): (query: GtxQuery) -> Gtv {
    val activeBlockchains = listOf(
            BlockchainInfo(
                    BlockchainRid.buildRepeat(10).wData,
                    "bc01",
                    BlockchainState.RUNNING,
                    "container01",
                    "cluster01",
                    false,
                    false,
                    false,
                    false,
                    null,
            ),
            BlockchainInfo(
                    BlockchainRid.buildRepeat(11).wData,
                    "bc02",
                    BlockchainState.RUNNING,
                    "container02",
                    "cluster01",
                    false,
                    false,
                    false,
                    false,
                    null,
            )
    )
    val inactiveBlockchains = listOf(
            BlockchainInfo(
                    BlockchainRid.buildRepeat(10).wData,
                    "bc03",
                    BlockchainState.PAUSED,
                    "container03",
                    "cluster01",
                    false,
                    false,
                    false,
                    false,
                    null,
            )
    )
    return { query ->
        val blockchains = activeBlockchains.toMutableList()
        if (query.args["include_inactive"]!!.asBoolean()) {
            blockchains += inactiveBlockchains
        }
        gtv(blockchains.map(GtvObjectMapper::toGtvDictionary))
    }
}

fun buildGetCompressedConfigurationParts(): Gtv {
    return gtv(listOf())
}

fun buildCmGetClusterInfoResponse(
        apiUrl: String = DEFAULT_NODE01_API,
        clusterAnchoringBrid: BlockchainRid = BlockchainRid.buildRepeat(99)
): Gtv {
    return GtvObjectMapper.toGtvDictionary(CmClusterInfo(
            "cluster01",
            clusterAnchoringBrid.wData,
            listOf(CmPeerInfo(DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray().wrap(), apiUrl)),
    ))
}

fun buildGetNodeDataResponse(apiUrl: String = DEFAULT_NODE01_API): Gtv {
    return GtvObjectMapper.toGtvDictionary(NodeData(
            DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray().wrap(),
            DEFAULT_NODE01_PUBKEY.wData,
            true,
            DEFAULT_NODE01_HOST,
            DEFAULT_NODE01_PORT,
            0L,
            apiUrl,
            1L,
            "EU",
            null
    ))
}

fun buildGetLastAnchoredBlockResponse(height: Long): Gtv {
    return GtvObjectMapper.toGtvDictionary(AnchorBlock(
            RowId(0),
            BlockchainRid.ZERO_RID.wData,
            height,
            0L,
            BlockchainRid.ZERO_RID.wData,
            ByteArray(0).wrap(),
            ByteArray(0).wrap(),
            0L
    ))
}

