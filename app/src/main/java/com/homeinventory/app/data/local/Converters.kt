package com.homeinventory.app.data.local

import androidx.room.TypeConverter
import com.homeinventory.app.data.models.InventoryCategory
import com.homeinventory.app.data.models.InventorySubcategory
import java.util.*

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

class InventoryCategoryConverter {
    @TypeConverter
    fun fromString(value: String?): InventoryCategory? {
        return value?.let { InventoryCategory.valueOf(value) }
    }

    @TypeConverter
    fun toString(category: InventoryCategory?): String? {
        return category?.name
    }
}

class InventorySubcategoryConverter {
    @TypeConverter
    fun fromString(value: String?): InventorySubcategory? {
        return value?.let { InventorySubcategory.valueOf(value) }
    }

    @TypeConverter
    fun toString(subcategory: InventorySubcategory?): String? {
        return subcategory?.name
    }
}
