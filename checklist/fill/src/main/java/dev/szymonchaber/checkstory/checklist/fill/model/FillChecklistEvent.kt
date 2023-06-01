package dev.szymonchaber.checkstory.checklist.fill.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId

sealed class FillChecklistEvent {

    object EditTemplateClicked : FillChecklistEvent()

    data class CreateChecklistFromTemplate(val templateId: TemplateId) : FillChecklistEvent()

    data class LoadChecklist(val checklistId: ChecklistId) : FillChecklistEvent()

    data class CheckChanged(val item: Task, val newCheck: Boolean) : FillChecklistEvent()

    data class NotesChanged(val notes: String) : FillChecklistEvent()

    data class ChildCheckChanged(val task: Task, val child: Task, val newCheck: Boolean) :
        FillChecklistEvent()

    object NotesClicked : FillChecklistEvent()

    object SaveChecklistClicked : FillChecklistEvent()

    object DeleteChecklistClicked : FillChecklistEvent()

    object ConfirmDeleteChecklistClicked : FillChecklistEvent()

    object BackClicked : FillChecklistEvent()

    object ConfirmExitClicked : FillChecklistEvent()
}
