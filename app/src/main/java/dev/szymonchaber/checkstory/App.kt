package dev.szymonchaber.checkstory

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.HiltAndroidApp
import dev.szymonchaber.checkstory.notifications.ReminderScheduler
import dev.szymonchaber.checkstory.notifications.ScheduleTodayRemindersReceiver
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject


@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    private val alarmManager by lazy { getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        runReminderSchedulerDaily()
    }

    private fun runReminderSchedulerDaily() {
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            LocalDateTime.now()
                .withHour(23)
                .withMinute(59)
                .toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))
                .toEpochMilli(),
            AlarmManager.INTERVAL_DAY,
            createIntent()
        )
    }

    private fun createIntent(): PendingIntent {
        return Intent(this, ScheduleTodayRemindersReceiver::class.java)
            .let {
                PendingIntent.getBroadcast(
                    this,
                    1000,
                    it,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
    }

}
