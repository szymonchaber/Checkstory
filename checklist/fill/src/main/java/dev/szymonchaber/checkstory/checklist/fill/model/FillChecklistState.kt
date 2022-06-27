package dev.szymonchaber.checkstory.checklist.fill.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist

data class FillChecklistState(val checklistLoadingState: ChecklistLoadingState) {

    companion object {

        val initial: FillChecklistState = FillChecklistState(ChecklistLoadingState.Loading)
    }
}

sealed interface ChecklistLoadingState {

    data class Success(private val originalChecklist: Checklist, val checklist: Checklist = originalChecklist) :
        ChecklistLoadingState {

        fun updateChecklist(block: Checklist.() -> Checklist): Success {
            return copy(checklist = checklist.block())
        }

        fun isChanged(): Boolean {
            return originalChecklist != checklist
        }
    }

    object Loading : ChecklistLoadingState
}
