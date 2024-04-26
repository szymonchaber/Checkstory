package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder

sealed interface EditTemplateEffect {

    data object CloseScreen : EditTemplateEffect

    data object ShowConfirmDeleteDialog : EditTemplateEffect

    data class ShowAddReminderSheet(val templateId: TemplateId) : EditTemplateEffect

    data class ShowEditReminderSheet(val reminder: Reminder) : EditTemplateEffect

    data object ShowConfirmExitDialog : EditTemplateEffect

    data object ShowFreeRemindersUsed : EditTemplateEffect

    data class OpenTemplateHistory(val templateId: TemplateId) : EditTemplateEffect
}
