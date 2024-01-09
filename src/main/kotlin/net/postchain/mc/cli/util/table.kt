package net.postchain.mc.cli.util

import com.chromia.cli.tools.formatter.defaultTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.table.ColumnWidth
import com.google.gson.GsonBuilder
import java.util.Locale

fun CliktCommand.pmcTable(name: String, headers: List<String>, rows: List<List<String>>, idColumn: Pair<Int, Int>? = null, interactive: Boolean = false): Any =
        if (terminal.info.outputInteractive)
            if (rows.isNotEmpty()) {
                prettyTable(name, headers, rows, idColumn, interactive)
            } else {
                "No $name"
            }
        else {
            jsonTable(headers, rows)
        }

private fun CliktCommand.prettyTable(name: String, headers: List<String>, rows: List<List<String>>, idColumn: Pair<Int, Int>?, interactive: Boolean) = defaultTable {
    captionTop("${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}:", TextAlign.LEFT)
    if (interactive) {
        column(0) {
            width = ColumnWidth.Fixed(3)
        }
    } else if (idColumn != null) {
        column(idColumn.first) {
            width = ColumnWidth.Fixed(idColumn.second + 2)
        }
    }
    header { rowFrom(if (interactive) listOf("#") + headers else headers) }
    body {
        rows.forEachIndexed { index, columns ->
            rowFrom(if (interactive) listOf(index.toString()) + columns else columns)
        }
    }
}

private fun jsonTable(headers: List<String>, rows: List<List<String>>): String {
    val table: List<Map<String, String>> = rows.map { row ->
        row.withIndex().associate { cell ->
            headers[cell.index].replace(' ', '_') to cell.value
        }
    }
    return GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(table)
}
