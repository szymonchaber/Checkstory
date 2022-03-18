package dev.szymonchaber.checkstory.checklist.fill.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist

data class FillChecklistState(val checklistLoadingState: ChecklistLoadingState) {

    companion object {

        val initial: FillChecklistState = FillChecklistState(ChecklistLoadingState.Loading)
    }
}

sealed interface ChecklistLoadingState {

    data class Success(val checklist: Checklist) : ChecklistLoadingState

    object Loading : ChecklistLoadingState
}
