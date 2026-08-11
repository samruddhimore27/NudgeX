package com.example.prepx.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing a single scheduled item in PrepX.
 */
@Entity(tableName = "planner_items")
data class PlannerItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val type: ItemType,
    val dateTime: Long, // Epoch milliseconds for event execution
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderTime: Long? = null, // Epoch milliseconds for exact alarm trigger
    val source: Source = Source.MANUAL,
    val externalId: String? = null // Codeforces contest ID for de-duplication
)
