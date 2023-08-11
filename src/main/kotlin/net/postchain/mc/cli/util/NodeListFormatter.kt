package net.postchain.mc.cli.util

import com.github.ajalt.mordant.table.Table
import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.mordant.rendering.Theme
import net.postchain.common.toHex
import net.postchain.gtv.Gtv
import java.time.Instant
import java.util.Date

object NodeListFormatter {

    fun renderNodes(theme: Theme, nodes: List<Array<out Gtv>>, includeInactive: Boolean): Table {
        return theme.defaultTable {
            header { row("pubkey", "host", "port", "active", "last updated") }
            body {
                nodes.forEach {
                    if (includeInactive || it[3].asBoolean()) {
                        row(
                                it[0].asByteArray().toHex(),
                                it[1].asString(),
                                it[2].asInteger().toString(),
                                it[3].asBoolean().toString(),
                                Date.from(Instant.ofEpochMilli(it[4].asInteger())).toString()
                        )
                    }
                }
            }
        }
    }
}