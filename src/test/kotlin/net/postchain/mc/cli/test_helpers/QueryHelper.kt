package net.postchain.mc.cli.test_helpers

import net.postchain.anchoring.anchoring_chain_common.AnchorBlock
import net.postchain.chain0.cm_api.CmClusterInfo
import net.postchain.chain0.cm_api.CmPeerInfo
import net.postchain.chain0.common.queries.BlockchainInfo
import net.postchain.chain0.common.queries.ContainerData
import net.postchain.chain0.common.queries.GetContainerBlockchainResult
import net.postchain.chain0.common.queries.GetMovingBlockchainInfoResult
import net.postchain.chain0.common.queries.GetUnarchivingBlockchainInfoResult
import net.postchain.chain0.common.queries.NodeData
import net.postchain.chain0.common.queries.SubnodeImageData
import net.postchain.chain0.model.BlockchainState
import net.postchain.chain0.model.ContainerState
import net.postchain.chain0.model.ImportingForeignBlockchain
import net.postchain.chain0.model.Provider
import net.postchain.chain0.model.ProviderTier
import net.postchain.chain0.model.SubnodeImageType
import net.postchain.chain0.proposal.GetRelevantProposalsResult
import net.postchain.chain0.proposal.ProposalState
import net.postchain.chain0.proposal.ProposalType
import net.postchain.common.BlockchainRid
import net.postchain.common.exception.UserMistake
import net.postchain.common.hexStringToByteArray
import net.postchain.common.toHex
import net.postchain.common.types.RowId
import net.postchain.common.wrap
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvArray
import net.postchain.gtv.GtvDictionary
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtx.GtxQuery
import net.postchain.mc.cli.test_helpers.ManagedRestTestApi.Companion.DEFAULT_PROVIDER_PUBKEY

val DEFAULT_PROVIDER01_PUBKEY = "03F7AB0AD49CC99773832549222E140603EF85B0904B78554BE5B87236712DF37E"
val DEFAULT_PROVIDER02_PUBKEY = "03F9ABC05F7D7639AEC97B18784D5C83CA82D1EAF8F96DC31E77A83F21DDE67F95"
val DEFAULT_PROVIDER01_PRIVKEY = "DC36585B89DD64D2F3A107FDA37C1730BEF3B3B13D5845B87C46EE235D0E9827"
val DEFAULT_PROVIDER02_PRIVKEY = "FFC28105CFE2CC336624DCDFDEDB58157B37ED565C29F11A3B54B8F721DBA7C5"

val DEFAULT_NODE01_PUBKEY = BlockchainRid.buildRepeat(50)
const val DEFAULT_NODE01_HOST = "host01"
const val DEFAULT_NODE01_PORT = 7740L
const val DEFAULT_NODE01_API = "http://$DEFAULT_NODE01_HOST:$DEFAULT_NODE01_PORT"

val DEFAULT_BRID_DIRECTORY_CHAIN = BlockchainRid.buildRepeat(1)
val DEFAULT_BRID_SYSTEM_ANCHRONING_CHAIN = BlockchainRid.buildRepeat(2)
val DEFAULT_BRID_CLUSTER_ANCHORING_CHAIN = BlockchainRid.buildRepeat(3)
val DEFAULT_BRID_ECONOMY_CHAIN = BlockchainRid.buildRepeat(4)

val DEFAULT_DAPP_RID = BlockchainRid.buildRepeat(33)

// Helper function to build a proposal list response
fun buildListProposalQueryResponse(pubkey: String, proposals: List<Triple<Long, ProposalType, ProposalState>>): (query: GtxQuery) -> Gtv {
    return { query ->
        val myPubkey = query.args.asDict()["my_pubkey"]!!.asByteArray()
        if (myPubkey.contentEquals(pubkey.hexStringToByteArray())) {
            gtv(
                    proposals.map {
                        GtvObjectMapper.toGtvDictionary(GetRelevantProposalsResult(
                                rowid = RowId(it.first),
                                proposalType = it.second,
                                state = it.third
                        ))
                    }
            )
        } else {
            throw UserMistake("Invalid pubkey: ${myPubkey.toHex()}")
        }
    }
}

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

fun buildGetContainerDataResponse(): Gtv {
    return GtvObjectMapper.toGtvDictionary(ContainerData(
            "container01",
            "cluster01",
            "deployer01",
            DEFAULT_PROVIDER_PUBKEY.hexStringToByteArray().wrap(),
            "proposed-by-name01",
            false,
            ContainerState.RUNNING,
            null
    ))
}

fun buildGetSubnodeImagesResponse(
        url: String = "gitlab.com/....",
): GtvArray {
    return gtv(listOf(SubnodeImageData(
            "image01",
            url,
            "sha256:abcdef123",
            SubnodeImageType.COMMON,
            ByteArray(0).wrap(),
            true,
            "Description of image01",
            "gtx_modules01",
            "sync_ext01",
    )).map(GtvObjectMapper::toGtvDictionary))
}

fun buildGetAllProvidersResponse(): GtvArray {
    return gtv(listOf(
            Provider(
                    DEFAULT_PROVIDER01_PUBKEY.hexStringToByteArray().wrap(),
                    "provider01",
                    "http://provider01:7740",
                    true,
                    ProviderTier.NODE_PROVIDER,
                    true
            ),
            Provider(
                    DEFAULT_PROVIDER02_PUBKEY.hexStringToByteArray().wrap(),
                    "provider02",
                    "http://provider02:7740",
                    true,
                    ProviderTier.NODE_PROVIDER,
                    true
            )
    ).map(GtvObjectMapper::toGtvDictionary))
}

fun buildGetSubnodeImageResponse(
        url: String = "gitlab.com/....",
): GtvDictionary {
    return GtvObjectMapper.toGtvDictionary(SubnodeImageData(
            "image01",
            url,
            "sha256:abcdef123",
            SubnodeImageType.COMMON,
            ByteArray(0).wrap(),
            true,
            "Description of image01",
            "gtx_modules01",
            "sync_ext01",
    ))
}

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

fun buildCmGetSystemAnchoringChainResponse(brid: BlockchainRid? = null): Gtv {
    return gtv((brid ?: BlockchainRid.buildRepeat(99)).wData)
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