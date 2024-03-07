package com.example.chatapplication.db

import androidx.room.TypeConverter
import java.util.Date

class Converter {
    @TypeConverter
    fun getLongFromDate(date: Date) : Long{
        return date.time;
    }

    @TypeConverter
    fun getDateFromLong(long: Long): Date {
        return Date(long)
    }
}