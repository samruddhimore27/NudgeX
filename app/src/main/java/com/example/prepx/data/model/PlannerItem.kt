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
    val externalId: String? = null, // Codeforces contest ID for de-duplication
    val repeatType: RepeatType = RepeatType.NONE,
    val repeatDays: String? = null, // Comma-separated days e.g. "FRI,SAT,SUN"
    val url: String? = null // Optional meeting/contest link e.g. Zoom/Meet/Contest URL
) {
    fun getRepeatSummary(): String? {
        return when (repeatType) {
            RepeatType.NONE -> null
            RepeatType.DAILY -> "🔄 Daily"
            RepeatType.WEEKLY -> {
                if (!repeatDays.isNullOrBlank()) {
                    val formattedDays = repeatDays.split(",")
                        .map { day ->
                            day.trim().lowercase().replaceFirstChar { it.uppercase() }
                        }
                        .joinToString(", ")
                    "🔄 Weekly ($formattedDays)"
                } else {
                    "🔄 Weekly"
                }
            }
        }
    }
}

