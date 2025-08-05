package com.coffestore.app.data

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    @TypeConverter
    fun fromIntList(intList: List<Int>): String {

        return intList.joinToString(",")
    }


    @TypeConverter
    fun toIntList(data: String): List<Int> {

        if (data.isEmpty()) {
            return emptyList()
        }

        return data.split(',').map { it.toInt() }
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

}
