package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import kotlinx.coroutines.flow.Flow

interface TemplateReminderRepository {

    suspend fun getAllReminders(): Flow<List<Reminder>>

    suspend fun deleteReminders(ids: List<ReminderId>)

    suspend fun getById(reminderId: ReminderId): Reminder?
}
