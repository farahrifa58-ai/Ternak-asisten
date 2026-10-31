package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val indonesianLocale = Locale("in", "ID")

    fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(indonesianLocale)
        formatter.maximumFractionDigits = 0
        return formatter.format(amount).replace("Rp", "Rp ")
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "-"
        val sdf = SimpleDateFormat("dd MMM yyyy", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp == 0L) return "-"
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            String.format(indonesianLocale, "%.2f", value)
        }
    }
}
