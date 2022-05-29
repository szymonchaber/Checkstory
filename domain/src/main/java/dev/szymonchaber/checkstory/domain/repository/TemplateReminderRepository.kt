package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import kotlinx.coroutines.flow.Flow

interface TemplateReminderRepository {

    suspend fun getAllReminders(): Flow<List<Reminder>>

    suspend fun deleteReminders(reminders: List<Reminder>)
}
