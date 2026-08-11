package com.example.prepx.data.db

import androidx.room.TypeConverter
import com.example.prepx.data.model.ItemType
import com.example.prepx.data.model.Source

/**
 * Room TypeConverters for custom enum data types.
 */
class Converters {

    @TypeConverter
    fun fromItemType(type: ItemType): String {
        return type.name
    }

    @TypeConverter
    fun toItemType(value: String): ItemType {
        return try {
            ItemType.valueOf(value)
        } catch (e: Exception) {
            ItemType.TASK
        }
    }

    @TypeConverter
    fun fromSource(source: Source): String {
        return source.name
    }

    @TypeConverter
    fun toSource(value: String): Source {
        return try {
            Source.valueOf(value)
        } catch (e: Exception) {
            Source.MANUAL
        }
    }
}
