package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import dev.szymonchaber.checkstory.domain.repository.TemplateReminderRepository
import javax.inject.Inject

class DeleteRemindersUseCase @Inject constructor(
    private val repository: TemplateReminderRepository
) {

    suspend fun deleteReminders(reminderIds: List<ReminderId>) {
        return repository.deleteReminders(reminderIds)
    }
}
