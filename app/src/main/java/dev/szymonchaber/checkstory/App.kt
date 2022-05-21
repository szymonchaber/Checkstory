package dev.szymonchaber.checkstory

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.szymonchaber.checkstory.notifications.ReminderScheduler
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
