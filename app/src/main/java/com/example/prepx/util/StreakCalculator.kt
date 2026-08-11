package com.example.prepx.util

import com.example.prepx.data.model.PlannerItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Utility for evaluating daily active streaks based on completed PlannerItems.
 */
object StreakCalculator {

    /**
     * Calculates the continuous consecutive daily streak of completed goals/tasks.
     * Uses java.time.LocalDate and ZoneId.systemDefault() for precise calendar day tracking.
     *
     * @param items List of completed PlannerItems.
     * @return Number of consecutive days completed without interruption.
     */
    fun calculateStreak(items: List<PlannerItem>): Int {
        val completedItems = items.filter { it.isCompleted }
        if (completedItems.isEmpty()) return 0

        // Extract distinct local dates when items were completed
        val completedDates: Set<LocalDate> = completedItems.map { item ->
            Instant.ofEpochMilli(item.dateTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.toSet()

        val today = LocalDate.now()
        var currentCheckDate = today
        var streakCount = 0

        // If nothing completed today, check if yesterday was completed to keep streak alive
        if (!completedDates.contains(today)) {
            val yesterday = today.minusDays(1)
            if (!completedDates.contains(yesterday)) {
                return 0
            }
            currentCheckDate = yesterday
        }

        // Count consecutive days backward
        while (completedDates.contains(currentCheckDate)) {
            streakCount++
            currentCheckDate = currentCheckDate.minusDays(1)
        }

        return streakCount
    }
}
