package net.postchain.mc.cli.economy

import com.chromia.build.tools.restapi.TestModel
import net.postchain.api.rest.controller.Model
import net.postchain.common.BlockchainRid
import net.postchain.gtv.GtvFactory
import net.postchain.gtx.GtxQuery

class D1TestModel(private val model: Model, private val ecBcrid: ByteArray) : Model by model {
    constructor(blockchainRid: BlockchainRid, ecBcrid: BlockchainRid) : this(TestModel(blockchainRid), ecBcrid.data)

    override fun query(query: GtxQuery) = when (query.name) {
        "api_version" -> GtvFactory.gtv(DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION)
        "get_economy_chain_rid" -> GtvFactory.gtv(ecBcrid)
        else -> throw IllegalArgumentException("Query not found: ${query.name}")
    }
}