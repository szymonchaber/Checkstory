package dev.szymonchaber.checkstory.checklist.history

data class ChecklistHistoryState(
    val historyLoadingState: HistoryLoadingState = HistoryLoadingState.Loading
) {

    companion object {

        val initial = ChecklistHistoryState()
    }
}

