package com.levelup.app.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE  // yyyy-MM-dd

    fun today(): String = LocalDate.now().format(formatter)

    fun parseDate(str: String): LocalDate = LocalDate.parse(str, formatter)

    fun formatDate(date: LocalDate): String = date.format(formatter)

    fun yesterday(): String = LocalDate.now().minusDays(1).format(formatter)
}
