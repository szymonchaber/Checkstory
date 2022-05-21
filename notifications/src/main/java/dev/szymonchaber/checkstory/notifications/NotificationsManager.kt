package dev.szymonchaber.checkstory.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class NotificationsManager @Inject constructor(@ApplicationContext val context: Context) {

    private val id = AtomicInteger(1)

    fun sendNotification(body: String, intent: Intent) {
        val requestCode = id.getAndIncrement()
        val pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.checkbox_marked)
            .setContentTitle("Checklist reminder") // TODO Extract to strings
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        createNotificationChannel()
        with(NotificationManagerCompat.from(context)) {
            notify(requestCode, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reminders"
//                context.getString(R.string.channel_name) // TODO Extract to strings as it's readable by the user
            val descriptionText = "Reminders"
//                context.getString(R.string.channel_description) // TODO Extract to strings as it's readable by the user
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    name,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = descriptionText
                }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {

        private const val CHANNEL_ID = "general"
    }
}