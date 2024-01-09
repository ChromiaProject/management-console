package net.postchain.mc.cli.util

import com.github.ajalt.clikt.core.CliktCommand
import net.postchain.common.toHex
import net.postchain.gtv.Gtv
import java.time.Instant
import java.util.Date

fun CliktCommand.renderNodes(name: String, nodes: List<Array<out Gtv>>, includeInactive: Boolean): Any = pmcTable(
        name,
        listOf("pubkey", "host", "port", "active", "last updated"),
        nodes.mapNotNull {
            if (includeInactive || it[3].asBoolean()) {
                listOf(
                        it[0].asByteArray().toHex(),
                        it[1].asString(),
                        it[2].asInteger().toString(),
                        it[3].asBoolean().toString(),
                        Date.from(Instant.ofEpochMilli(it[4].asInteger())).toString()
                )
            } else null
        }
)
