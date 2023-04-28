package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.dao.ReminderDao
import dev.szymonchaber.checkstory.data.database.model.reminder.ReminderEntity
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import dev.szymonchaber.checkstory.domain.repository.TemplateReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TemplateReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : TemplateReminderRepository {

    override suspend fun getAllReminders(): Flow<List<Reminder>> {
        return reminderDao
            .getAll()
            .map {
                withContext(Dispatchers.Default) {
                    it.map(ReminderEntity::toDomainReminder)
                }
            }
    }

    override suspend fun deleteReminders(ids: List<ReminderId>) {
        withContext(Dispatchers.Default) {
            ids.map { it.id }
                .forEach {
                    reminderDao.deleteById(it)
                }
        }
    }
}
