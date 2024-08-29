package com.cubicj.coffeenote

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.time.Instant

class Converters {
    private val moshi = Moshi.Builder().build()

    // List<Long> 변환
    private val listLongAdapter = moshi.adapter<List<Long>>(
        Types.newParameterizedType(List::class.java, Long::class.javaObjectType)
    )

    @TypeConverter
    fun fromListLong(value: List<Long>?): String? {
        return listLongAdapter.toJson(value)
    }

    @TypeConverter
    fun toListLong(value: String?): List<Long>? {
        return value?.let { listLongAdapter.fromJson(it) }
    }

    // Map<String, Int> 변환
    private val mapStringIntAdapter = moshi.adapter<Map<String, Int>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Int::class.javaObjectType)
    )

    @TypeConverter
    fun fromMapStringInt(value: Map<String, Int>?): String? {
        return mapStringIntAdapter.toJson(value)
    }

    @TypeConverter
    fun toMapStringInt(value: String?): Map<String, Int>? {
        return value?.let { mapStringIntAdapter.fromJson(it) }
    }

    // List<String> 변환
    private val listStringType = Types.newParameterizedType(List::class.java, String::class.java)
    private val listStringAdapter = moshi.adapter<List<String>>(listStringType)

    @TypeConverter
    fun fromStringList(value: String): List<String>? {
        return listStringAdapter.fromJson(value)
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return listStringAdapter.toJson(list)
    }

    // IntListConverter 통합
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return value.split(",").mapNotNull { it.toIntOrNull() }
    }

    // DateConverter 통합
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun toTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun fromLongArray(value: LongArray): String {
        return value.joinToString(",") // LongArray를 쉼표로 구분된 문자열로 변환
    }

    @TypeConverter
    fun toLongArray(value: String): LongArray {
        return value.split(",").mapNotNull { it.toLongOrNull() }.toLongArray() // 문자열을 LongArray로 변환
    }
}