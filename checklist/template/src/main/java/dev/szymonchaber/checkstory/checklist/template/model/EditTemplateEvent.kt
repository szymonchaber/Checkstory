package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox

sealed interface EditTemplateEvent {

    object CreateChecklistTemplate : EditTemplateEvent

    data class EditChecklistTemplate(val checklistTemplateId: ChecklistTemplateId) : EditTemplateEvent

    data class TitleChanged(val newTitle: String) : EditTemplateEvent

    data class DescriptionChanged(val newDescription: String) : EditTemplateEvent

    data class ItemRemoved(val templateCheckbox: TemplateCheckbox) : EditTemplateEvent

    data class ItemTitleChanged(val checkbox: TemplateCheckbox, val newTitle: String) : EditTemplateEvent

    object AddCheckboxClicked : EditTemplateEvent

    object SaveTemplateClicked : EditTemplateEvent
}
