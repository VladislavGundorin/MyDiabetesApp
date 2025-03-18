package com.example.mydiabetesapp.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.mydiabetesapp.R

class MyNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("notification_message") ?: "Напоминание"
        val withSound = intent.getBooleanExtra("withSound", true)
        val autoCancel = intent.getBooleanExtra("autoCancel", true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "mydiabetes_channel_id"
        val channelName = "MyDiabetes Notifications"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            if (withSound) {
                channel.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
            } else {
                channel.setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Напоминание")
            .setContentText(message)
            .setAutoCancel(autoCancel)

        if (withSound) {
            builder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
        }

        val notification = builder.build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
