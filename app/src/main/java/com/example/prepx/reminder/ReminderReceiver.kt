package com.example.prepx.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.prepx.R
import com.example.prepx.ui.main.MainActivity

/**
 * BroadcastReceiver triggered by AlarmManager for 1-hour contest alerts, custom task reminders, and morning digests.
 * Plays loud sound and phone vibration.
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PrepX_ReminderReceiver"
        const val CHANNEL_ID = "prepx_high_priority_reminders"
        const val CHANNEL_NAME = "PrepX Contest & Task Reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast action: ${intent.action}")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device rebooted. Re-scheduling morning digest.")
            AlarmScheduler.scheduleDailyMorningDigest(context)
            return
        }

        val itemId = intent.getLongExtra(AlarmScheduler.EXTRA_ITEM_ID, System.currentTimeMillis() / 1000L)
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_ITEM_TITLE) ?: "PrepX Contest Alert"
        val itemType = intent.getStringExtra(AlarmScheduler.EXTRA_ITEM_TYPE) ?: "CONTEST"

        val notificationTitle = when (itemType) {
            "CONTEST" -> "🏆 Contest Alert: Upcoming Contest!"
            "MORNING_DIGEST" -> "📅 PrepX Daily Contest Alert"
            else -> "⏰ PrepX Reminder: $itemType"
        }

        val notificationContent = when (itemType) {
            "CONTEST" -> "Give contest! '$title' is starting soon. Get ready!"
            "MORNING_DIGEST" -> title
            else -> title
        }

        showLoudNotification(context, itemId, notificationTitle, notificationContent)
    }

    private fun showLoudNotification(context: Context, itemId: Long, title: String, contentText: String) {
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

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
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
            .build()

        notificationManager.notify(itemId.toInt(), notification)
        Log.d(TAG, "Loud notification displayed for id=$itemId title='$title'")
    }
}
