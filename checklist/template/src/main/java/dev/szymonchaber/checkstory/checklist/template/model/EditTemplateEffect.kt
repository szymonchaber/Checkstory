package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder

sealed interface EditTemplateEffect {

    object CloseScreen : EditTemplateEffect

    @Suppress("CanSealedSubClassBeObject")
    class ShowConfirmDeleteDialog : EditTemplateEffect

    @Suppress("CanSealedSubClassBeObject")
    class ShowAddReminderSheet : EditTemplateEffect

    class ShowEditReminderSheet(val reminder: Reminder) : EditTemplateEffect

    @Suppress("CanSealedSubClassBeObject")
    class ShowConfirmExitDialog : EditTemplateEffect
}
