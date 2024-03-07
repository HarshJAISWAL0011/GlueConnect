package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object util {

    fun getHourAndMinuteFromTimestamp(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val min = String.format("%02d", calendar.get(Calendar.MINUTE))
        val period = if (hour < 12) "AM" else "PM"
        val formattedHour = if (hour % 12 == 0) 12 else hour % 12
        return "$formattedHour:$min $period"
    }

    fun formatTime(timestamp: Long?): String {
        val calendar = Calendar.getInstance().apply {
            if (timestamp != null) {
                timeInMillis = timestamp
            }
        }
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

}