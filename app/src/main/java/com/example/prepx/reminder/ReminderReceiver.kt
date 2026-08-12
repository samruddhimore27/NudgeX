package com.example.prepx.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.prepx.R
import com.example.prepx.data.db.AppDatabase
import com.example.prepx.data.model.RepeatType
import com.example.prepx.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver triggered by AlarmManager for 1-hour contest alerts, custom task reminders, and morning digests.
 * Plays loud sound and phone vibration.
 * Supports interactive notification action buttons ("Mark as Done", "Join Class", "Give Contest").
 * Automatically reschedules next occurrences for daily & weekly repeating tasks.
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PrepX_ReminderReceiver"
        const val CHANNEL_ID = "prepx_high_priority_reminders"
        const val CHANNEL_NAME = "NudgeX Contest & Task Reminders"
        const val ACTION_MARK_DONE = "com.example.prepx.ACTION_MARK_DONE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast action: ${intent.action}")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device rebooted. Re-scheduling morning digest and active task reminders.")
            AlarmScheduler.scheduleDailyMorningDigest(context)
            rescheduleActiveReminders(context)
            return
        }

        if (intent.action == ACTION_MARK_DONE) {
            val itemId = intent.getLongExtra(AlarmScheduler.EXTRA_ITEM_ID, -1L)
            Log.d(TAG, "Notification action 'MARK_DONE' received for itemId=$itemId")
            if (itemId != -1L) {
                markItemCompleted(context, itemId)
            }
            return
        }

        val itemId = intent.getLongExtra(AlarmScheduler.EXTRA_ITEM_ID, -1L)
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_ITEM_TITLE) ?: "PrepX Contest Alert"
        val itemType = intent.getStringExtra(AlarmScheduler.EXTRA_ITEM_TYPE) ?: "CONTEST"
        val itemUrl = intent.getStringExtra(AlarmScheduler.EXTRA_ITEM_URL)

        val notificationTitle = when (itemType) {
            "CONTEST" -> "🏆 NudgeX Contest Alert!"
            "MORNING_DIGEST" -> "📅 NudgeX Daily Digest"
            else -> "⏰ NudgeX Reminder: $itemType"
        }

        val notificationContent = when (itemType) {
            "CONTEST" -> "Give contest! '$title' is starting soon. Get ready!"
            "MORNING_DIGEST" -> title
            else -> title
        }

        val notificationId = if (itemId != -1L) itemId else System.currentTimeMillis() / 1000L
        showLoudNotification(context, notificationId, notificationTitle, notificationContent, itemType, itemUrl)

        if (itemId != -1L) {
            rescheduleNextOccurrence(context, itemId)
        }
    }

    private fun markItemCompleted(context: Context, itemId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(itemId.toInt())

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                db.plannerDao().updateCompletionStatus(itemId, true)
                Log.d(TAG, "Successfully marked item id=$itemId as completed directly from notification!")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark item completed from notification: ${e.localizedMessage}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun rescheduleNextOccurrence(context: Context, itemId: Long) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val item = db.plannerDao().getItemById(itemId)
                if (item != null && item.reminderEnabled && item.repeatType != RepeatType.NONE) {
                    AlarmScheduler.scheduleExactReminder(context, item)
                    Log.d(TAG, "Successfully auto-rescheduled next occurrence for repeating item '${item.title}'")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling next occurrence: ${e.localizedMessage}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun rescheduleActiveReminders(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val activeItems = db.plannerDao().getActiveReminderItems()
                for (item in activeItems) {
                    AlarmScheduler.scheduleExactReminder(context, item)
                }
                Log.d(TAG, "Rescheduled ${activeItems.size} active task reminders after reboot.")
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling active reminders: ${e.localizedMessage}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showLoudNotification(
        context: Context,
        itemId: Long,
        title: String,
        contentText: String,
        itemType: String,
        itemUrl: String?
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)

        // Create High-Priority Notification Channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Loud buzzing notifications for 1-hour contest reminders and morning digests"
                enableVibration(true)
                this.vibrationPattern = vibrationPattern
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            itemId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val appIconBitmap = try {
            android.graphics.BitmapFactory.decodeResource(context.resources, com.example.prepx.R.mipmap.ic_launcher)
        } catch (e: Exception) {
            null
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.example.prepx.R.mipmap.ic_launcher)
            .setLargeIcon(appIconBitmap)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(vibrationPattern)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Add Notification Action Button: "Mark as Done" / "Mark Joined"
        if (itemId > 0L) {
            val markDoneIntent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_MARK_DONE
                putExtra(AlarmScheduler.EXTRA_ITEM_ID, itemId)
            }
            val markDonePendingIntent = PendingIntent.getBroadcast(
                context,
                (itemId + 100000L).toInt(),
                markDoneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val markDoneLabel = if (itemType == "CLASS") "✓ Mark Joined" else "✓ Mark as Done"
            builder.addAction(R.drawable.ic_notification, markDoneLabel, markDonePendingIntent)
        }

        // Add Notification Action Button: "Join Class" / "Give Contest" / "Open Link"
        if (!itemUrl.isNullOrBlank()) {
            try {
                val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(itemUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val linkPendingIntent = PendingIntent.getActivity(
                    context,
                    (itemId + 200000L).toInt(),
                    linkIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val linkLabel = when (itemType) {
                    "CLASS" -> "🎥 Join Class"
                    "CONTEST" -> "🏆 Give Contest"
                    else -> "🔗 Open Link"
                }
                builder.addAction(R.drawable.ic_notification, linkLabel, linkPendingIntent)
            } catch (e: Exception) {
                Log.w(TAG, "Could not parse notification URL '$itemUrl': ${e.localizedMessage}")
            }
        }

        notificationManager.notify(itemId.toInt(), builder.build())
        Log.d(TAG, "Loud notification displayed for id=$itemId title='$title'")
    }
}
