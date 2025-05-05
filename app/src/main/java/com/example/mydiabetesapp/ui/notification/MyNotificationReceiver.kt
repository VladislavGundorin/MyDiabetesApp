package com.example.mydiabetesapp.ui.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.mydiabetesapp.R
import com.example.mydiabetesapp.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val message       = intent.getStringExtra("notification_message") ?: "Напоминание"
        val withSound     = intent.getBooleanExtra("withSound", true)
        val autoCancel    = intent.getBooleanExtra("autoCancel", true)
        val repeatDaily   = intent.getBooleanExtra("repeatDaily", false)
        val intervalMin   = intent.getLongExtra("intervalMinutes", 0L)
        val requestCode   = intent.getIntExtra("requestCode", 0)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "mydiabetes_channel_id"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "MyDiabetes Notifications", NotificationManager.IMPORTANCE_HIGH)
            if (withSound) channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, null)
            nm.createNotificationChannel(channel)
        }
        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Напоминание")
            .setContentText(message)
            .setAutoCancel(autoCancel)
            .apply { if (withSound) setSound(Settings.System.DEFAULT_NOTIFICATION_URI) }
            .build()
        nm.notify(requestCode, notif)

        if (repeatDaily || intervalMin > 0) {
            scheduleNext(context, message, withSound, autoCancel, repeatDaily, intervalMin, requestCode)
        }

        if (!repeatDaily && intervalMin == 0L && autoCancel) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, MyNotificationReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(pi)

            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getDatabase(context)
                    .notificationDao()
                    .deleteNotification(requestCode)
            }
        }
    }

    private fun scheduleNext(
        context: Context,
        message: String,
        withSound: Boolean,
        autoCancel: Boolean,
        repeatDaily: Boolean,
        intervalMin: Long,
        requestCode: Int
    ) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            return
        }

        val nextIntent = Intent(context, MyNotificationReceiver::class.java).apply {
            putExtra("notification_message", message)
            putExtra("withSound", withSound)
            putExtra("autoCancel", autoCancel)
            putExtra("repeatDaily", repeatDaily)
            putExtra("intervalMinutes", intervalMin)
            putExtra("requestCode", requestCode)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextTrigger = if (repeatDaily) {
            System.currentTimeMillis() + AlarmManager.INTERVAL_DAY
        } else {
            System.currentTimeMillis() + intervalMin * 60_000
        }

        try {
            am.setExact(AlarmManager.RTC_WAKEUP, nextTrigger, pi)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
