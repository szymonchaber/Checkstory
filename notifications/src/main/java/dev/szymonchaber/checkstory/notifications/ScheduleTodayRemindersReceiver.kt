package dev.szymonchaber.checkstory.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleTodayRemindersReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) = Unit
}
