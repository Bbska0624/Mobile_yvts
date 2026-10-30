package com.example.financetracker.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val DATE_PATTERN = "yyyy.MM.dd"

fun todayDate(): String = SimpleDateFormat(DATE_PATTERN, Locale.US).format(Date())

fun millisToDate(millis: Long): String =
    SimpleDateFormat(DATE_PATTERN, Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(millis))

fun dateToMillis(date: String): Long? =
    try {
        SimpleDateFormat(DATE_PATTERN, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }.parse(date)?.time
    } catch (e: Exception) {
        null
    }

fun formatAmount(amount: Long): String = String.format(Locale.US, "%,d ₮", amount)
