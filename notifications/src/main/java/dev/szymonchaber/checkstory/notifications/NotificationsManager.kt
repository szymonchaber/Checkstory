package dev.szymonchaber.checkstory.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.szymonchaber.checkstory.design.R
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsManager @Inject constructor(@ApplicationContext val context: Context) {

    fun sendReminderNotification(
        reminderId: ReminderId,
        body: String,
        newChecklistIntent: Intent,
        recentChecklistIntent: Intent?
    ) {
        val requestCode = reminderId.id.hashCode()
        val newChecklistPendingIntent =
            PendingIntent.getActivity(context, requestCode, newChecklistIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, REMINDERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.checkbox_marked)
            .setContentTitle(context.getString(R.string.notification_reminder_title))
            .setContentText(body)
            .setContentIntent(newChecklistPendingIntent)
            .addAction(
                NotificationCompat.Action.Builder(
                    null,
                    context.getString(R.string.fill_new_checklist),
                    newChecklistPendingIntent
                ).build()
            )
            .apply {
                recentChecklistIntent?.let {
                    PendingIntent.getActivity(context, requestCode, it, PendingIntent.FLAG_IMMUTABLE)
                }?.let {
                    addAction(
                        NotificationCompat.Action.Builder(
                            null,
                            context.getString(R.string.complete_recent_checklist),
                            it
                        ).build()
                    )
                }
            }
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        createNotificationChannels()
        with(NotificationManagerCompat.from(context)) {
            notify(requestCode, builder.build())
        }
    }

    fun createSynchronizationNotification(): Notification {
        createNotificationChannels()
        return NotificationCompat.Builder(context, SYNCHRONIZATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.checkbox_marked)
            .setContentTitle(context.getString(R.string.synchronization_ongoing_title))
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(
                context.getString(R.string.reminder_channel_name),
                context.getString(R.string.reminder_channel_description),
                REMINDERS_CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            createChannel(
                context.getString(R.string.synchronization_channel_name),
                context.getString(R.string.synchronization_channel_description),
                SYNCHRONIZATION_CHANNEL_ID,
                NotificationManager.IMPORTANCE_LOW
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(name: String, description: String, channelId: String, importance: Int) {
        val channel = NotificationChannel(
            channelId,
            name,
            importance
        ).apply {
            this.description = description
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {

        private const val REMINDERS_CHANNEL_ID = "reminders"
        private const val SYNCHRONIZATION_CHANNEL_ID = "synchronization"
    }
}
