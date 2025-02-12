package net.postchain.mc.cli.blockchain.import_chain

import com.chromia.build.tools.restapi.TestModel
import net.postchain.api.rest.controller.Model
import net.postchain.api.rest.model.ApiStatus
import net.postchain.api.rest.model.TxRid
import net.postchain.common.BlockchainRid
import net.postchain.common.tx.TransactionStatus
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtx.GtxQuery
import net.postchain.mc.cli.BaseTransactionHandler

class D1TestFinishImportModel(private val model: Model, private val apiUrl: String, private val configurationHeights: Map<Long, Gtv>) : Model by model, BaseTransactionHandler() {
    constructor(blockchainRid: BlockchainRid, apiUrl: String, configurationHeights: Map<Long, Gtv>) : this(TestModel(blockchainRid), apiUrl, configurationHeights)
    override fun query(query: GtxQuery) = when (query.name) {
        "api_version" -> gtv(20L)
        "cm_get_blockchain_api_urls" -> gtv(gtv(apiUrl))

        "nm_find_next_configuration_height" -> configurationHeights[query.args["height"]?.asInteger()]!!
        else -> throw IllegalArgumentException("Query not found: ${query.name}")
    }

    override fun postTransaction(tx: ByteArray) {
        postTransactionImpl(tx)
    }

    override fun getStatus(txRID: TxRid): ApiStatus {
        return ApiStatus(TransactionStatus.CONFIRMED)
    }
}