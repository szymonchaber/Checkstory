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

        createNotificationChannel()
        with(NotificationManagerCompat.from(context)) {
            notify(requestCode, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.reminder_channel_name)
            val descriptionText = context.getString(R.string.reminder_channel_description)
            val channel =
                NotificationChannel(
                    REMINDERS_CHANNEL_ID,
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

        private const val REMINDERS_CHANNEL_ID = "reminders"
    }
}
