package net.postchain.mc.cli.economy

import com.chromia.build.tools.restapi.TestModel
import net.postchain.api.rest.controller.Model
import net.postchain.chain0.common.queries.GetSummaryResult
import net.postchain.common.BlockchainRid
import net.postchain.common.hexStringToByteArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtx.GtxQuery

class D1TestModel(private val model: Model, private val apiUrl: String, private val ecBcrid: ByteArray) : Model by model {
    constructor(blockchainRid: BlockchainRid, apiUrl: String, ecBcrid: BlockchainRid) : this(TestModel(blockchainRid), apiUrl, ecBcrid.data)

    override fun query(query: GtxQuery) = when (query.name) {
        "api_version" -> gtv(DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION)
        "cm_get_blockchain_api_urls" -> gtv(gtv(apiUrl))
        "cm_get_blockchain_cluster" -> gtv("system")
        "cm_get_cluster_info" -> {
            val clusterName = query.args["name"]!!.asString()
            if (clusterName == "system")
                gtv(mapOf(
                        "name" to gtv(clusterName),
                        "anchoring_chain" to gtv("".hexStringToByteArray()),
                        "peers" to gtv(gtv(mapOf(
                                "pubkey" to gtv("".hexStringToByteArray()),
                                "api_url" to gtv(apiUrl)
                        )))
                ))
            else
                throw IllegalArgumentException("Cluster not found: $clusterName")
        }
        "get_economy_chain_rid" -> gtv(ecBcrid)
        "get_summary" -> GtvObjectMapper.toGtvDictionary(GetSummaryResult(12, 1, 2, 5, 10, 230))
        else -> throw IllegalArgumentException("Query not found: ${query.name}")
    }
}