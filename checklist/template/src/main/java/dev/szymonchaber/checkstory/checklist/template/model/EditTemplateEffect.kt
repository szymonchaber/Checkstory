package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder

sealed interface EditTemplateEffect {

    object CloseScreen : EditTemplateEffect

    @Suppress("CanSealedSubClassBeObject")
    class ShowConfirmDeleteDialog : EditTemplateEffect

    class ShowAddReminderSheet(val templateId: TemplateId) : EditTemplateEffect

    class ShowEditReminderSheet(val reminder: Reminder) : EditTemplateEffect

    data object ShowConfirmExitDialog : EditTemplateEffect

    @Suppress("CanSealedSubClassBeObject")
    class ShowFreeRemindersUsed : EditTemplateEffect

    data class OpenTemplateHistory(val templateId: TemplateId) : EditTemplateEffect

    @Suppress("CanSealedSubClassBeObject")
    class ShowTryDraggingSnackbar : EditTemplateEffect
}
