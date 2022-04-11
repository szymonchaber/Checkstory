package dev.szymonchaber.checkstory.checklist.history

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId

sealed interface ChecklistHistoryEffect {

    data class NavigateToFillChecklistScreen(val checklistId: ChecklistId) : ChecklistHistoryEffect
}
