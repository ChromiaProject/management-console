package net.postchain.mc.cli.base

import com.github.ajalt.mordant.table.RowHolderBuilder

fun RowHolderBuilder.rowIfNotNull(vararg cells: Any?) {
    if (cells.filterNotNull().size == cells.size) {
        row(*cells)
    }
}

fun RowHolderBuilder.rowIfNotNull(nullable: Any?, cellsFunction: () -> List<Any>) {
    if (nullable != null) {
        row(*cellsFunction().toTypedArray())
    }
}
