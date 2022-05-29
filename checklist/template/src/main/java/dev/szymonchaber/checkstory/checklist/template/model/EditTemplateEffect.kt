package dev.szymonchaber.checkstory.checklist.template.model

sealed interface EditTemplateEffect {

    object CloseScreen : EditTemplateEffect

    @Suppress("CanSealedSubClassBeObject")
    class ShowAddReminderSheet : EditTemplateEffect
}
