package dev.szymonchaber.checkstory.checklist.fill.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

sealed class FillChecklistEvent {

    data class CreateChecklistFromTemplate(val checklistTemplateId: ChecklistTemplateId) : FillChecklistEvent()

    data class LoadChecklist(val checklistId: String) : FillChecklistEvent()

    data class CheckChanged(val item: Checkbox, val newCheck: Boolean) : FillChecklistEvent()

    data class NotesChanged(val notes: String) : FillChecklistEvent()
}
