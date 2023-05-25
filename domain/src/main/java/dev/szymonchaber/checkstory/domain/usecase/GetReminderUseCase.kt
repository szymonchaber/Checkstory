package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import dev.szymonchaber.checkstory.domain.repository.TemplateReminderRepository
import javax.inject.Inject

class GetReminderUseCase @Inject constructor(
    private val repository: TemplateReminderRepository
) {

    suspend fun getReminder(reminderId: ReminderId): Reminder? {
        return repository.getById(reminderId)
    }
}
