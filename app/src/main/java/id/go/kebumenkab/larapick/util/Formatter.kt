package id.go.kebumenkab.larapick.util

import java.text.SimpleDateFormat
import java.util.Locale

fun dateFormatter(date: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    val outputFormat = SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.US)

    val dateFormat = inputFormat.parse(date)
    return outputFormat.format(dateFormat!!)
}