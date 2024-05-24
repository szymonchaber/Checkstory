package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import dev.szymonchaber.checkstory.domain.repository.TemplateReminderRepository
import dev.szymonchaber.checkstory.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TemplateReminderRepositoryImpl @Inject constructor(
    private val templateRepository: TemplateRepository
) : TemplateReminderRepository {

    override fun getAllReminders(): Flow<List<Reminder>> {
        return templateRepository.getAll()
            .map {
                it.flatMap(Template::reminders)
            }
    }

    override suspend fun getById(reminderId: ReminderId): Reminder? {
        return templateRepository.getAll()
            .first()
            .flatMap(Template::reminders)
            .firstOrNull { it.id == reminderId }
    }
}
