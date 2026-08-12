package com.example.prepx.reminder

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.prepx.data.model.PlannerItem
import com.example.prepx.data.model.RepeatType
import java.util.Calendar
import java.util.TimeZone

/**
 * Manages scheduling exact wall-clock alarms and morning contest digests.
 */
object AlarmScheduler {

    private const val TAG = "PrepX_Alarm"
    const val ACTION_EXACT_REMINDER = "com.example.prepx.ACTION_EXACT_REMINDER"
    const val ACTION_MORNING_DIGEST = "com.example.prepx.ACTION_MORNING_DIGEST"
    const val EXTRA_ITEM_ID = "extra_item_id"
    const val EXTRA_ITEM_TITLE = "extra_item_title"
    const val EXTRA_ITEM_TYPE = "extra_item_type"
    const val EXTRA_ITEM_URL = "extra_item_url"

    /**
     * Calculates the next upcoming epoch millis for an exact reminder trigger.
     * Supports ONE-TIME (NONE), DAILY, and WEEKLY on specific days.
     */
    fun calculateNextReminderTime(item: PlannerItem, nowMs: Long = System.currentTimeMillis()): Long? {
        val baseOffsetMs = if (item.reminderTime != null) {
            item.dateTime - item.reminderTime
        } else {
            3600000L // 1 hour default
        }

        val baseCal = Calendar.getInstance().apply {
            timeInMillis = item.dateTime
        }
        val hourOfDay = baseCal.get(Calendar.HOUR_OF_DAY)
        val minute = baseCal.get(Calendar.MINUTE)
        val second = baseCal.get(Calendar.SECOND)

        return when (item.repeatType) {
            RepeatType.NONE -> {
                val targetReminderTime = item.reminderTime ?: (item.dateTime - baseOffsetMs)
                if (targetReminderTime > nowMs) targetReminderTime else null
            }
            RepeatType.DAILY -> {
                val targetCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, second)
                    set(Calendar.MILLISECOND, 0)
                }
                var targetReminderTime = targetCal.timeInMillis - baseOffsetMs
                while (targetReminderTime <= nowMs) {
                    targetCal.add(Calendar.DAY_OF_YEAR, 1)
                    targetReminderTime = targetCal.timeInMillis - baseOffsetMs
                }
                targetReminderTime
            }
            RepeatType.WEEKLY -> {
                val rawDays = item.repeatDays?.split(",")?.map { it.trim().uppercase() } ?: emptyList()
                val targetDaysOfWeek = rawDays.mapNotNull { dayStr ->
                    when (dayStr) {
                        "SUN" -> Calendar.SUNDAY
                        "MON" -> Calendar.MONDAY
                        "TUE" -> Calendar.TUESDAY
                        "WED" -> Calendar.WEDNESDAY
                        "THU" -> Calendar.THURSDAY
                        "FRI" -> Calendar.FRIDAY
                        "SAT" -> Calendar.SATURDAY
                        else -> null
                    }
                }.toSet().ifEmpty { setOf(baseCal.get(Calendar.DAY_OF_WEEK)) }

                val targetCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, second)
                    set(Calendar.MILLISECOND, 0)
                }

                var candidateReminderTime: Long? = null
                for (i in 0..14) {
                    val dayOfWeek = targetCal.get(Calendar.DAY_OF_WEEK)
                    if (targetDaysOfWeek.contains(dayOfWeek)) {
                        val reminderTimeMs = targetCal.timeInMillis - baseOffsetMs
                        if (reminderTimeMs > nowMs) {
                            candidateReminderTime = reminderTimeMs
                            break
                        }
                    }
                    targetCal.add(Calendar.DAY_OF_YEAR, 1)
                }
                candidateReminderTime
            }
        }
    }

    /**
     * Schedules a 1-hour prior alarm or custom reminder for a PlannerItem.
     */
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleExactReminder(context: Context, item: PlannerItem) {
        val reminderTime = calculateNextReminderTime(item)
        if (reminderTime == null) {
            Log.d(TAG, "Reminder time is in the past or no future occurrence for item '${item.title}'. Skipping schedule.")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_EXACT_REMINDER
            putExtra(EXTRA_ITEM_ID, item.id)
            putExtra(EXTRA_ITEM_TITLE, item.title)
            putExtra(EXTRA_ITEM_TYPE, item.type.name)
            putExtra(EXTRA_ITEM_URL, item.url)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled exact alarm for '${item.title}' (${item.repeatType}) at epoch millis: $reminderTime")
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled fallback alarm for '${item.title}' (${item.repeatType}) at epoch millis: $reminderTime")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm: ${e.localizedMessage}", e)
        }
    }

    /**
     * Schedules a daily morning alert at 08:00 AM to remind the user about today's contests.
     */
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleDailyMorningDigest(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = Calendar.getInstance()
        val morningCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (morningCal.before(now)) {
            morningCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_MORNING_DIGEST
            putExtra(EXTRA_ITEM_ID, 999999L)
            putExtra(EXTRA_ITEM_TITLE, "Check PrepX today! Contests and study goals scheduled.")
            putExtra(EXTRA_ITEM_TYPE, "MORNING_DIGEST")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            999999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                morningCal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            Log.d(TAG, "Scheduled daily morning digest alarm for 08:00 AM.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule morning digest: ${e.localizedMessage}")
        }
    }

    /**
     * Cancels an existing exact alarm for a given item ID.
     */
    fun cancelReminder(context: Context, itemId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_EXACT_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            itemId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for item id=$itemId")
        }
    }
}
