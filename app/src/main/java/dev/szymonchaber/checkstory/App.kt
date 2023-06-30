package dev.szymonchaber.checkstory

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import dev.szymonchaber.checkstory.common.LogStorage
import dev.szymonchaber.checkstory.data.migration.CommandModelMigration
import dev.szymonchaber.checkstory.data.synchronization.SynchronizationWorker
import dev.szymonchaber.checkstory.domain.usecase.GetCurrentUserUseCase
import dev.szymonchaber.checkstory.domain.usecase.PushFirebaseMessagingTokenUseCase
import dev.szymonchaber.checkstory.notifications.ReminderScheduler
import dev.szymonchaber.checkstory.notifications.ScheduleTodayRemindersReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    @Inject
    lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    @Inject
    lateinit var pushFirebaseMessagingTokenUseCase: PushFirebaseMessagingTokenUseCase

    @Inject
    lateinit var logStorage: LogStorage

    @Inject
    lateinit var commandModelMigration: CommandModelMigration

    private val alarmManager by lazy { getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
//        plantProductionDebugTree()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        migrateToCommandModel()
        setPaymentTierProperty()
        runReminderSchedulerDaily()
        synchronizeDataPeriodically()
        fetchFirebaseToken()
    }

    private fun migrateToCommandModel() {
        GlobalScope.launch {
            commandModelMigration.run()
        }
    }

    private fun fetchFirebaseToken() {
        GlobalScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                pushFirebaseMessagingTokenUseCase.pushFirebaseMessagingToken(token)
                Timber.d("Firebase messaging token: $token")
            } catch (exception: Exception) {
                Timber.e("Fetching FCM registration token failed", exception)
            }
        }
    }

    private fun synchronizeDataPeriodically() {
        GlobalScope.launch {
            SynchronizationWorker.scheduleRepeating(WorkManager.getInstance(this@App))
        }
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
                val tier = if (getCurrentUserUseCase.getCurrentUser().isPaidUser) {
                    SUBSCRIPTION_TIER_PRO
                } else {
                    SUBSCRIPTION_TIER_FREE
                }
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
