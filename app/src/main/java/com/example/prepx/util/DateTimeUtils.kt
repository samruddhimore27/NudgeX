package com.example.prepx.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Date and time formatting utilities for humanized UI displays and ISO string parsing.
 */
object DateTimeUtils {

    /**
     * Formats epoch millisecond timestamp into standard display format e.g. "Aug 15, 2026 - 02:30 PM".
     */
    fun formatDateTime(epochMillis: Long): String {
        if (epochMillis <= 0) return "N/A"
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }

    /**
     * Formats epoch millisecond timestamp into short date format e.g. "Aug 15".
     */
    fun formatShortDate(epochMillis: Long): String {
        if (epochMillis <= 0) return "N/A"
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }

    /**
     * Formats epoch millisecond timestamp into time format e.g. "02:30 PM".
     */
    fun formatTime(epochMillis: Long): String {
        if (epochMillis <= 0) return "N/A"
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }

    /**
     * Formats duration in seconds into human-readable hours/minutes string e.g. "2h 30m".
     */
    fun formatDuration(durationSeconds: Long): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    /**
     * Parses ISO 8601 timestamp string (e.g. "2026-08-15T14:30:00.000Z", "2026-07-29T20:00:00+05:30", "2026-08-15 14:30:00") to epoch milliseconds.
     */
    fun parseIsoToEpochMillis(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return 0L

        // 1. Try java.time OffsetDateTime
        try {
            return OffsetDateTime.parse(dateStr).toInstant().toEpochMilli()
        } catch (e: Exception) {
            // Continue
        }

        // 2. Try java.time ZonedDateTime
        try {
            return ZonedDateTime.parse(dateStr).toInstant().toEpochMilli()
        } catch (e: Exception) {
            // Continue
        }

        // 3. Try Instant.parse
        try {
            return Instant.parse(dateStr).toEpochMilli()
        } catch (e: Exception) {
            // Continue
        }

        // 4. Fallback SimpleDateFormat parsing
        val formats = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss 'UTC'",
            "yyyy-MM-dd HH:mm:ss",
            "dd MMM yyyy HH:mm:ss"
        )

        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(dateStr)
                if (date != null) return date.time
            } catch (e: Exception) {
                // Continue to next format
            }
        }

        return 0L
    }
}
