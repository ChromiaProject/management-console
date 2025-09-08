package net.postchain.mc.cli.test_helpers

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.fail
import net.postchain.api.rest.BlockHeight
import net.postchain.api.rest.controller.Model
import net.postchain.api.rest.model.ApiStatus
import net.postchain.api.rest.model.TxRid
import net.postchain.client.config.PostchainClientConfig
import net.postchain.client.impl.PostchainClientImpl
import net.postchain.client.request.EndpointPool
import net.postchain.client.transaction.TransactionBuilder
import net.postchain.common.BlockchainRid
import net.postchain.common.tx.TransactionStatus
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvDecoder
import net.postchain.gtv.GtvFactory
import net.postchain.gtx.GtxQuery
import java.util.LinkedList
import java.util.Queue


/**
 * A generic test model for the Rest API to mock queries and capture operation calls.
 */
open class RestTestModel(
        override val chainIID: Long,
        val model: Model,
        val dynamicQueries: MutableMap<String, Queue<(query: GtxQuery) -> Gtv>> = mutableMapOf(),
        var height: Long = 0
) : Model by model {

    val capturedOps: MutableMap<String, MutableList<List<Gtv>>> = mutableMapOf()

    open fun withQuery(query: String, vararg arg: Gtv): RestTestModel {
        val functionList: List<(query: GtxQuery) -> Gtv> = arg.map { { _ -> it } }
        withQuery(query, *functionList.toTypedArray())
        return this
    }

    open fun withQuery(query: String, function: (query: GtxQuery) -> Gtv): RestTestModel {
        dynamicQueries[query] = LinkedList(listOf(function))
        return this
    }

    open fun withQuery(query: String, vararg function: (query: GtxQuery) -> Gtv): RestTestModel {
        dynamicQueries[query] = LinkedList(listOf(*function))
        return this
    }

    override fun query(query: GtxQuery): Gtv {
        val queries = dynamicQueries[query.name]
        return queries?.peek()?.let {
            if (queries.size > 1) {
                queries.poll()
            }
            it(query)
        } ?: throw IllegalArgumentException("Query not found: ${query.name}")
    }

    override fun postTransaction(tx: ByteArray) {
        val txGtv = GtvFactory.decodeGtv(tx)
        for (op in txGtv.asArray()) {
            if (op.asArray().size > 1) {
                for (op1 in op[1].asArray()) {
                    val name = op1[0].asString()
                    val parameters = op1[1].asArray().toList()
                    capturedOps.compute(name) { _, list ->
                        val newList = list ?: mutableListOf()
                        newList.add(parameters)
                        newList
                    }
                }
            }
        }
    }

    fun assertSingleOp(name: String, parameters: List<Gtv>) {

        val opCalls = capturedOps[name]

        assertThat(opCalls?.size).isEqualTo(1)
        assertThat(opCalls?.get(0)).isEqualTo(parameters)
    }

    override fun getStatus(txRID: TxRid): ApiStatus {
        return ApiStatus(TransactionStatus.CONFIRMED)
    }

    // Assert that ops was called by constructing them with the TransactionBuilder.
    fun assertCalledOps(opsProvider: (TransactionBuilder) -> Unit) {

        val transactionBuilder = PostchainClientImpl(PostchainClientConfig(
                BlockchainRid.ZERO_RID,
                EndpointPool.singleUrl(""),
                merkleHashVersion = 2,
        )).transactionBuilder()

        opsProvider(transactionBuilder)

        val txData = transactionBuilder.build()
        GtvDecoder.decodeGtv(txData).asArray()
                .filter { it.asArray().size > 1 }
                .forEach { tx ->
                    tx.asArray().drop(1).forEach() { ops ->
                        ops.asArray().forEach { op ->
                            val name = op[0].asString()
                            val parameters = op[1].asArray().toList()
                            if (!opWasCalled(name, parameters)) {
                                fail("Operation never called: $name with parameters $parameters but found: ${
                                    capturedOps.map { capturedOp ->
                                        "${capturedOp.key}(${capturedOp.value})"
                                    }
                                }")
                            }
                        }
                    }
                }
    }

    fun opWasCalled(name: String, parameters: List<Gtv>): Boolean {
        val ops = capturedOps[name] ?: return false
        return ops.any {
            it.size == parameters.size && it.zip(parameters).all { (a, b) -> a == b }
        }
    }

    fun opWasCalled(name: String, parameterFilter: (parameters: List<Gtv>) -> Boolean): Boolean {
        val ops = capturedOps[name] ?: return false
        return ops.any(parameterFilter)
    }

    override fun getCurrentBlockHeight(): BlockHeight {
        return BlockHeight(height)
    }
}