package net.postchain.mc.cli

import assertk.assertThat
import assertk.assertions.isEqualTo
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvFactory

open class BaseTransactionHandler {
    val ops: MutableMap<String, MutableList<List<Gtv>>> = mutableMapOf()

    protected fun postTransactionImpl(tx: ByteArray) {
        val txGtv = GtvFactory.decodeGtv(tx)
        for (op in txGtv.asArray()) {
            if (op.asArray().size > 1) {
                for (op1 in op[1].asArray()) {
                    val name = op1[0].asString()
                    val parameters = op1[1].asArray().toList()
                    ops.compute(name) { _, list ->
                        val newList = list ?: mutableListOf()
                        newList.add(parameters)
                        newList
                    }
                }
            }
        }
    }

    fun assertSingleOp(name: String, parameters: List<Gtv>) {

        val opCalls = ops[name]

        assertThat(opCalls?.size).isEqualTo(1)
        assertThat(opCalls?.get(0)).isEqualTo(parameters)
    }

}