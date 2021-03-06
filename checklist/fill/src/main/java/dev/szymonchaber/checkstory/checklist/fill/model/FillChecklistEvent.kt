package dev.szymonchaber.checkstory.checklist.fill.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

sealed class FillChecklistEvent {

    object EditTemplateClicked : FillChecklistEvent()

    data class CreateChecklistFromTemplate(val checklistTemplateId: ChecklistTemplateId) : FillChecklistEvent()

    data class LoadChecklist(val checklistId: ChecklistId) : FillChecklistEvent()

    data class CheckChanged(val item: Checkbox, val newCheck: Boolean) : FillChecklistEvent()

    data class NotesChanged(val notes: String) : FillChecklistEvent()

    data class ChildCheckChanged(val checkbox: Checkbox, val child: Checkbox, val newCheck: Boolean) :
        FillChecklistEvent()

    object SaveChecklistClicked : FillChecklistEvent()

    object DeleteChecklistClicked : FillChecklistEvent()
}
