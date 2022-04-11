package dev.szymonchaber.checkstory.checklist.history

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

sealed interface ChecklistHistoryEvent {

    data class LoadChecklistHistory(val templateId: ChecklistTemplateId) : ChecklistHistoryEvent

    data class ChecklistClicked(val checklistId: ChecklistId) : ChecklistHistoryEvent
}
