package net.postchain.mc.cli.test_helpers

import net.postchain.api.rest.controller.Model
import net.postchain.chain0.common.queries.GetSummaryResult
import net.postchain.common.BlockchainRid
import net.postchain.common.hexStringToByteArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper

class D1TestModel(
        model: Model,
        ecBcRid: ByteArray,
        chainIID: Long = 0,
        dcVersion: Long = 1,
        sacBcRid: BlockchainRid,
        providerByKey: String? = null,
) : RestTestModel(chainIID, model) {

    init {
        withQuery("api_version", gtv(dcVersion))
        withQuery("get_economy_chain_rid", gtv(ecBcRid))
        withQuery("get_summary", GtvObjectMapper.toGtvDictionary(GetSummaryResult(12, 1, 2, 5, 10, 230)))
        withQuery("get_relevant_proposals", gtv(listOf()))
        withQuery("cm_get_system_anchoring_chain", gtv(sacBcRid.wData))
        if (providerByKey != null) {
            withQuery("get_provider_by_key", gtv(providerByKey.hexStringToByteArray()))
            withQuery("get_provider_keys_and_threshold",
                    gtv(mapOf("keys" to gtv(listOf(gtv(providerByKey.hexStringToByteArray()))), "threshold" to gtv(1))))
        }
    }

    fun setBlockchainApiUrlsByQuery(apiUrl: String) {
        withQuery("cm_get_blockchain_api_urls", gtv(gtv(apiUrl)))
    }
}