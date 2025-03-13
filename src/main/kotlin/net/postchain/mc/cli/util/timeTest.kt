package net.postchain.mc.cli.util

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

val DATE_TIME_FORMATS = listOf(
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd'T'HH:mm",
).associateWith { DateTimeFormatter.ofPattern(it) }

fun parseDateTime(time: String, formats: List<DateTimeFormatter> = DATE_TIME_FORMATS.values.toList()): LocalDateTime? {
    for (formatter in formats) {
        try {
            return LocalDateTime.parse(time, formatter)
        } catch (_: DateTimeParseException) {
        }
    }
    return null
}

fun parseDateTimeAsEpochMillis(time: String, formats: List<DateTimeFormatter> = DATE_TIME_FORMATS.values.toList()): Long? {
    return parseDateTime(time, formats)?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
}