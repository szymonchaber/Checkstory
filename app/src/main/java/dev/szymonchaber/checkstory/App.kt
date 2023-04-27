package dev.szymonchaber.checkstory

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import dev.szymonchaber.checkstory.common.LogStorage
import dev.szymonchaber.checkstory.domain.usecase.IsProUserUseCase
import dev.szymonchaber.checkstory.notifications.ReminderScheduler
import dev.szymonchaber.checkstory.notifications.ScheduleTodayRemindersReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject


@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    @Inject
    lateinit var isProUserUseCase: IsProUserUseCase

    @Inject
    lateinit var logStorage: LogStorage

    private val alarmManager by lazy { getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    override fun onCreate() {
        super.onCreate()
//        plantProductionDebugTree()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        setPaymentTierProperty()
        runReminderSchedulerDaily()
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.w("Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
//            Timber.d("Token: ${task.result}")
        })
    }

    private fun plantProductionDebugTree() {
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                if (message.length < MAX_LOG_LENGTH) {
                    logStorage.append("$tag: $message")
                    return
                }

                // Split by line, then ensure each line can fit into Log's maximum length.
                var i = 0
                val length = message.length
                while (i < length) {
                    var newline = message.indexOf('\n', i)
                    newline = if (newline != -1) newline else length
                    do {
                        val end = Math.min(newline, i + MAX_LOG_LENGTH)
                        val part = message.substring(i, end)
                        logStorage.append("$tag: $part")
                        i = end
                    } while (i < newline)
                    i++
                }
            }
        })
    }

    private fun setPaymentTierProperty() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val tier = if (isProUserUseCase.isProUser()) SUBSCRIPTION_TIER_PRO else SUBSCRIPTION_TIER_FREE
                FirebaseAnalytics.getInstance(this@App).setUserProperty(SUBSCRIPTION_TIER, tier)
            }
        }
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

    companion object {

        private const val SUBSCRIPTION_TIER = "subscription_tier"
        private const val SUBSCRIPTION_TIER_FREE = "free"
        private const val SUBSCRIPTION_TIER_PRO = "pro"

        private const val MAX_LOG_LENGTH = 4000
    }
}
