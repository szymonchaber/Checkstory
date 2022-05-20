package dev.szymonchaber.checkstory

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.szymonchaber.checkstory.notifications.ReminderScheduler
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var reminderScheduler: ReminderScheduler
}
