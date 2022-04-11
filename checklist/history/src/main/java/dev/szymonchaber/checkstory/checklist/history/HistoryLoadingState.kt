package dev.szymonchaber.checkstory.checklist.history

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist

sealed interface HistoryLoadingState {

    object Loading : HistoryLoadingState

    data class Success(val checklists: List<Checklist>) : HistoryLoadingState
}
