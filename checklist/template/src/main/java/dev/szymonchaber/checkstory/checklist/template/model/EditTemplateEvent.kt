package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.checklist.template.EditTemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

sealed interface EditTemplateEvent {

    object CreateChecklistTemplate : EditTemplateEvent

    data class EditChecklistTemplate(val checklistTemplateId: ChecklistTemplateId) : EditTemplateEvent

    data class TitleChanged(val newTitle: String) : EditTemplateEvent

    data class DescriptionChanged(val newDescription: String) : EditTemplateEvent

    data class ItemRemoved(val checkbox: EditTemplateCheckbox) : EditTemplateEvent

    data class ItemTitleChanged(val checkbox: EditTemplateCheckbox, val newTitle: String) : EditTemplateEvent

    object AddCheckboxClicked : EditTemplateEvent

    object SaveTemplateClicked : EditTemplateEvent

    object DeleteTemplateClicked : EditTemplateEvent
}
