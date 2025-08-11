package com.homeinventory.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var content: String,
    val createdDate: Date = Date(),
    var lastModified: Date = Date()
)
